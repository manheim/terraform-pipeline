import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlanOnlyPluginTest {
    @BeforeEach
    void resetJenkinsEnv() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    @Nested
    public class Init {
        @AfterEach
        void resetPlugins() {
            TerraformPlanCommand.reset()
            TerraformEnvironmentStage.reset()
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            PlanOnlyPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PlanOnlyPlugin.class)))
        }

        @Test
        void addsParameter() {
            PlanOnlyPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.BooleanParameterDefinition',
                name: "FAIL_PLAN_ON_CHANGES",
                defaultValue: false,
                description: 'Plan run with -detailed-exitcode; ANY CHANGES will cause failure'
            ]))
        }
    }

    @Nested
    public class Apply {

        @Test
        void decoratesTheTerraformEnvironmentStage()  {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            plugin.apply(environment)

            verify(environment, times(1)).decorateAround(eq(TerraformEnvironmentStage.CONFIRM), any(Closure.class))
            verify(environment, times(1)).decorateAround(eq(TerraformEnvironmentStage.APPLY), any(Closure.class))
        }

        @Test
        void addsArgumentToTerraformPlan() {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()
            configureJenkins(env: [
                'FAIL_PLAN_ON_CHANGES': 'true'
            ])

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-detailed-exitcode"))
            assertThat(result, containsString("set -e; set -o pipefail"))
        }

        @Test
        void doesNotAddArgumentToTerraformPlan() {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()
            configureJenkins(env: [
                'FAIL_PLAN_ON_CHANGES': 'false'
            ])

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-detailed-exitcode")))
            assertThat(result, not(containsString("set -e; set -o pipefail")))
        }
    }

}
