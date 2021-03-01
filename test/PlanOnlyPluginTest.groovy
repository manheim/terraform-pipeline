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

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class PlanOnlyPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            PlanOnlyPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PlanOnlyPlugin.class)))
        }

        @Test
        void addsFailPlanOnChangesParameter() {
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

        @Test
        void addsPlanOnlyParameter() {
            PlanOnlyPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.BooleanParameterDefinition',
                name: "PLAN_ONLY",
                defaultValue: false,
                description: 'Run `terraform plan` only, skipping `terraform apply`.'
            ]))
        }
    }

    @Nested
    public class Apply {

        @Test
        void decoratesTheTerraformEnvironmentStageWhenPlanOnlyTrue()  {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            MockJenkinsfile.withEnv('PLAN_ONLY': 'true')
            plugin.apply(environment)

            verify(environment, times(1)).decorateAround(eq(TerraformEnvironmentStage.CONFIRM), any(Closure.class))
            verify(environment, times(1)).decorateAround(eq(TerraformEnvironmentStage.APPLY), any(Closure.class))
        }

        @Test
        void doesNotDecorateTheTerraformEnvironmentStageWhenPlanOnlyTrue()  {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            MockJenkinsfile.withEnv('PLAN_ONLY': 'false')
            plugin.apply(environment)

            verify(environment, times(0)).decorateAround(eq(TerraformEnvironmentStage.CONFIRM), any(Closure.class))
            verify(environment, times(0)).decorateAround(eq(TerraformEnvironmentStage.APPLY), any(Closure.class))
        }

        @Test
        void addsFailPlanOnChangesArgumentToTerraformPlan() {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()
            MockJenkinsfile.withEnv('FAIL_PLAN_ON_CHANGES': 'true')

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-detailed-exitcode"))
            assertThat(result, containsString("set -e; set -o pipefail"))
        }

        @Test
        void doesNotAddFailPlanOnChangesArgumentToTerraformPlan() {
            PlanOnlyPlugin plugin = new PlanOnlyPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()
            MockJenkinsfile.withEnv('FAIL_PLAN_ON_CHANGES': 'false')

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-detailed-exitcode")))
            assertThat(result, not(containsString("set -e; set -o pipefail")))
        }
    }

}
