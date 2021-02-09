import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class ParameterStoreExecPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            ParameterStoreExecPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreExecPlugin.class)))
        }

        @Test
        void modifiesTerraformPlanCommand() {
            ParameterStoreExecPlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreExecPlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            ParameterStoreExecPlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreExecPlugin.class)))
        }
    }

    @Nested
    public class Apply {
        @Test
        void addsParameterStorePrefixToTerraformPlan() {
            ParameterStoreExecPlugin plugin = new ParameterStoreExecPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("parameter-store-exec terraform plan"))
        }

        @Test
        void addsParameterStorePrefixToTerraformApply() {
            ParameterStoreExecPlugin plugin = new ParameterStoreExecPlugin()
            TerraformApplyCommand command = new TerraformApplyCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("parameter-store-exec terraform apply"))
        }
    }

    @Nested
    public class PathForEnvironment {
        @Test
        void constructPathUsingOrgRepoAndEnvironment() {
            String organization = 'SomeOrg'
            String repoName = 'SomeRepo'
            String environment = "qa"

            MockJenkinsfile.withOrganization(organization).withRepoName(repoName)
            ParameterStoreExecPlugin plugin = new ParameterStoreExecPlugin()

            String actual = plugin.pathForEnvironment(environment)
            assertThat(actual, equalTo("/${organization}/${repoName}/${environment}/".toString()))
        }
    }

}

