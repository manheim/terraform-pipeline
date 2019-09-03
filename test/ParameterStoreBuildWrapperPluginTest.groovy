import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.hamcrest.Matchers.*

@RunWith(HierarchicalContextRunner.class)
class ParameterStoreBuildWrapperPluginTest {
    public class Init {
        @After
        void resetPlugins() {
            TerraformEnvironmentStage.resetPlugins()
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            ParameterStoreBuildWrapperPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }
    }

    public class PathForEnvironment {
        @After
        public void reset() {
            Jenkinsfile.instance = null
        }

        private configureJenkins(Map config = [:]) {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
            when(Jenkinsfile.instance.getRepoName()).thenReturn(config.repoName ?: 'repo')
            when(Jenkinsfile.instance.getOrganization()).thenReturn(config.organization ?: 'org')
            when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
        }

        @Test
        void constructPathUsingOrgRepoAndEnvironment() {
            String organization = "MyOrg"
            String repoName = "MyRepo"
            String environment = "qa"

            configureJenkins(repoName: repoName, organization: organization)
            ParameterStoreBuildWrapperPlugin plugin = new ParameterStoreBuildWrapperPlugin()

            String actual = plugin.pathForEnvironment(environment)
            assertEquals(actual, "/${organization}/${repoName}/${environment}/".toString())
        }
    }
}

