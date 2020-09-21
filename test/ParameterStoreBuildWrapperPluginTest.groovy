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
            TerraformValidateStage.resetPlugins()
            TerraformEnvironmentStage.reset()
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            ParameterStoreBuildWrapperPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStageCommandWithGlobalParameter() {
            ParameterStoreBuildWrapperPlugin.withGlobalParameter('/somePath/').init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStageCommandWithGlobalParameterAndOptions() {
            ParameterStoreBuildWrapperPlugin.withGlobalParameter('/somePath/', [someKey: true, anotherKey: 'someValue']).init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }

        @Test
        void modifiesTerraformValidateStageCommand() {
            ParameterStoreBuildWrapperPlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }

        @Test
        void modifiesTerraformValidateStageCommandWithGlobalParameter() {
            ParameterStoreBuildWrapperPlugin.withGlobalParameter('/somePath/').init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }

        @Test
        void modifiesTerraformValidateStageCommandWithGlobalParameterAndOptions() {
            ParameterStoreBuildWrapperPlugin.withGlobalParameter('/somePath/', [someKey: true, anotherKey: 'someValue']).init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }
    }

    public class PathForEnvironment {
        @After
        public void reset() {
            Jenkinsfile.instance = null
            ParameterStoreBuildWrapperPlugin.reset()
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
            assertEquals("/${organization}/${repoName}/${environment}/".toString(), actual)
        }

        @Test
        void usesCustomPatternWhenProvided() {
            String organization = "MyOrg"
            String repoName = "MyRepo"
            String environment = "qa"
            Closure customPattern = { options -> "/foo/${options['organization']}/${options['environment']}/${options['repoName']}" }

            configureJenkins(repoName: repoName, organization: organization)
            ParameterStoreBuildWrapperPlugin.withPathPattern(customPattern)
            ParameterStoreBuildWrapperPlugin plugin = new ParameterStoreBuildWrapperPlugin()

            String actual = plugin.pathForEnvironment(environment)
            assertEquals("/foo/${organization}/${environment}/${repoName}".toString(), actual)
        }
    }

    class WithPathPattern {
        @After
        public void reset() {
            ParameterStoreBuildWrapperPlugin.reset()
        }

        @Test
        void isFluent() {
            def result = ParameterStoreBuildWrapperPlugin.withPathPattern { options -> 'somePattern' }

            assertEquals(ParameterStoreBuildWrapperPlugin.class, result)
        }
    }

    class withGlobalParameter {
        @After
        public void reset() {
            ParameterStoreBuildWrapperPlugin.reset()
        }

        @Test
        void addGlobalParameter() {
            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter('/path/', [])

            assertEquals([[path: '/path/']], result.globalParameterOptions)
        }

        @Test
        void addGlobalParameterWithOptions() {
            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter('/path/', [recursive: true, basename: 'relative'])

            assertEquals([[path: '/path/', recursive: true, basename: 'relative']], result.globalParameterOptions)
        }

        @Test
        void addMulitpleGlobalParameters() {
            ArrayList expected = []
            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter('/path/', [])
                                                         .withGlobalParameter('/path2/', [recursive: true])
                                                         .withGlobalParameter('/path3/', [basename: 'something'])
            expected << [path:'/path/']
            expected << [path: '/path2/', recursive: true]
            expected << [path: '/path3/', basename: 'something']
            assertEquals(expected, result.globalParameterOptions)
        }
    }
}

