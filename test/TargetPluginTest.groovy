import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TargetPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformPlanCommand() {
            TargetPlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TargetPlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            TargetPlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TargetPlugin.class)))
        }

        @Test
        void addsParameter() {
            TargetPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.StringParameterDefinition',
                name: "RESOURCE_TARGETS",
                defaultValue: '',
                description: 'comma-separated list of resource addresses to pass to plan and apply "-target=" parameters'
            ]))
        }
    }

    @Nested
    public class Apply {
        @Test
        void addsTargetArgumentToTerraformPlan() {
            MockJenkinsfile.withEnv(
                'RESOURCE_TARGETS': 'aws_dynamodb_table.test-table-2,aws_dynamodb_table.test-table-3'
            )
            TargetPlugin plugin = new TargetPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(" -target aws_dynamodb_table.test-table-2 -target aws_dynamodb_table.test-table-3"))
        }

        @Test
        void doesNotAddTargetArgumentToTerraformPlanWhenResourceTargetsBlank() {
            MockJenkinsfile.withEnv('RESOURCE_TARGETS': '')
            TargetPlugin plugin = new TargetPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-target")))
        }

        @Test
        void addsTargetArgumentToTerraformApply() {
            MockJenkinsfile.withEnv('RESOURCE_TARGETS': 'aws_dynamodb_table.test-table-2,aws_dynamodb_table.test-table-3')
            TargetPlugin plugin = new TargetPlugin()
            TerraformApplyCommand command = new TerraformApplyCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(" -target aws_dynamodb_table.test-table-2 -target aws_dynamodb_table.test-table-3"))
        }

        @Test
        void doesNotAddTargetArgumentToTerraformApplyWhenResourceTargetsBlank() {
            MockJenkinsfile.withEnv('RESOURCE_TARGETS': '')
            TargetPlugin plugin = new TargetPlugin()
            TerraformApplyCommand command = new TerraformApplyCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-target")))
        }

    }

}
