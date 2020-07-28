import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class PlanOnlyPluginTest {
    @Before
    void resetJenkinsEnv() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    public class Init {
        @After
        void resetPlugins() {
            TerraformPlanCommand.resetPlugins()
            TerraformEnvironmentStage.resetPlugins()
        }

        @Test
        void modifiesTerraformPlanCommand() {
            PlanOnlyPlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PlanOnlyPlugin.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            PlanOnlyPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PlanOnlyPlugin.class)))

            Collection actualParms = TerraformEnvironmentStage.getParams()
            assertThat(actualParms, hasItem([
                $class: 'hudson.model.BooleanParameterDefinition',
                name: "FAIL_PLAN_ON_CHANGES",
                defaultValue: false,
                description: 'Plan run with -detailed-exitcode; ANY CHANGES will cause failure'
            ]))
        }
    }

    public class Apply {

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
        }

        @Test
        void setStrategyForTerraformEnvironmentStage() {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            def environment = spy(new TerraformEnvironmentStage())

            plugin.apply(environment)

            verify(environment, times(1)).withStrategy(any(PlanOnlyStrategy.class))
        }
    }

}
