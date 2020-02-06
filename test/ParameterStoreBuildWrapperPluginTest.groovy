import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

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

