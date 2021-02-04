import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TargetPluginTest {
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
            TerraformApplyCommand.reset()
        }

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
            TargetPlugin plugin = new TargetPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()
            configureJenkins(env: [
                'RESOURCE_TARGETS': 'aws_dynamodb_table.test-table-2,aws_dynamodb_table.test-table-3'
            ])

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(" -target aws_dynamodb_table.test-table-2 -target aws_dynamodb_table.test-table-3"))
        }

        @Test
        void doesNotAddTargetArgumentToTerraformPlanWhenResourceTargetsBlank() {
            TargetPlugin plugin = new TargetPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()
            configureJenkins(env: [
                'RESOURCE_TARGETS': ''
            ])

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-target")))
        }

        @Test
        void addsTargetArgumentToTerraformApply() {
            TargetPlugin plugin = new TargetPlugin()
            TerraformApplyCommand command = new TerraformApplyCommand()
            configureJenkins(env: [
                'RESOURCE_TARGETS': 'aws_dynamodb_table.test-table-2,aws_dynamodb_table.test-table-3'
            ])

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(" -target aws_dynamodb_table.test-table-2 -target aws_dynamodb_table.test-table-3"))
        }

        @Test
        void doesNotAddTargetArgumentToTerraformApplyWhenResourceTargetsBlank() {
            TargetPlugin plugin = new TargetPlugin()
            TerraformApplyCommand command = new TerraformApplyCommand()
            configureJenkins(env: [
                'RESOURCE_TARGETS': ''
            ])

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-target")))
        }

    }

}
