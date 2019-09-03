import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.*

@RunWith(HierarchicalContextRunner.class)
class ParameterStoreExecPluginTest {
    @After
    public void reset() {
        Jenkinsfile.instance = null
        TerraformEnvironmentStage.resetPlugins()
        TerraformPlanCommand.resetPlugins()
        TerraformApplyCommand.resetPlugins()
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getOrganization()).thenReturn(config.organization)
        when(Jenkinsfile.instance.getRepoName()).thenReturn(config.repoName)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

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

    public class PathForEnvironment {
        @Test
        void constructPathUsingOrgRepoAndEnvironment() {
            String organization = 'SomeOrg'
            String repoName = 'SomeRepo'
            String environment = "qa"

            configureJenkins(organization: organization, repoName: repoName)
            ParameterStoreExecPlugin plugin = new ParameterStoreExecPlugin()

            String actual = plugin.pathForEnvironment(environment)
            assertEquals(actual, "/${organization}/${repoName}/${environment}/".toString())
        }
    }

}

