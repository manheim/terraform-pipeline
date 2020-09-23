import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.never
import static org.mockito.Mockito.times
import static org.mockito.Mockito.anyString

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
        void modifiesTerraformValidateStageCommand() {
            ParameterStoreBuildWrapperPlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ParameterStoreBuildWrapperPlugin.class)))
        }
    }

    public class Apply {
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

        class WithTerraformValidateStage {
            @Test
            void doesNotDecorateTheTerraformValidateStageIfGlobalParametersNotSet() {
                def expectedClosure                     = { -> }
                TerraformValidateStage stage            = mock(TerraformValidateStage.class)
                ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

                plugin.apply(stage)

                verify(stage, never()).decorate(expectedClosure)
            }

            @Test
            void decorateTheTerraformValidateStageIfGlobalParametersSet() {
                String path                             = '/somePath/'
                def expectedClosure                     = { -> }
                Map gp                                  = [path: path]
                TerraformValidateStage stage            = mock(TerraformValidateStage.class)
                ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

                doReturn(expectedClosure).when(plugin).addParameterStoreBuildWrapper(gp)

                plugin.withGlobalParameter(path)
                plugin.apply(stage)

                verify(stage).decorate(TerraformValidateStage.ALL, expectedClosure)
            }
        }

        class WithTerraformEnvironmentStage {
            @Test
            void decorateTheTerraformEnvironmentStageIfGlobalParametersNotSet() {
                String organization = "MyOrg"
                String repoName     = "MyRepo"
                String environment  = "MyEnv"
                Map apo             = [path: "/${organization}/${repoName}/${environment}/", credentialsId: "${environment.toUpperCase()}_PARAMETER_STORE_ACCESS"]
                def expectedClosure = { -> }
                configureJenkins(repoName: repoName, organization: organization)

                TerraformEnvironmentStage stage         = mock(TerraformEnvironmentStage.class)
                ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

                doReturn(environment).when(stage).getEnvironment()
                doReturn(apo).when(plugin).getEnvironmentParameterOptions(environment)
                doReturn(expectedClosure).when(plugin).addParameterStoreBuildWrapper(apo)

                plugin.apply(stage)

                verify(stage, times(2)).decorate(anyString(), eq(expectedClosure))
            }

            @Test
            void decorateTheTerraformEnvironmentStageWhenGlobalParametersSet() {
                String organization = "MyOrg"
                String repoName     = "MyRepo"
                String environment  = "MyEnv"
                String path         = '/someOtherPath/'
                Map apo             = [path: "/${organization}/${repoName}/${environment}/", credentialsId: "${environment.toUpperCase()}_PARAMETER_STORE_ACCESS"]
                Map gp              = [path: path]
                def firstClosure    = { -> }
                def secondClosure   = { -> }
                configureJenkins(repoName: repoName, organization: organization)

                TerraformEnvironmentStage stage         = mock(TerraformEnvironmentStage.class)
                ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

                doReturn(environment).when(stage).getEnvironment()
                doReturn(apo).when(plugin).getEnvironmentParameterOptions(environment)
                doReturn(firstClosure).when(plugin).addParameterStoreBuildWrapper(gp)
                doReturn(secondClosure).when(plugin).addParameterStoreBuildWrapper(apo)

                plugin.withGlobalParameter(path)
                plugin.apply(stage)

                verify(stage, times(2)).decorate(anyString(), eq(firstClosure))
                verify(stage, times(2)).decorate(anyString(), eq(secondClosure))
            }
        }
    }

    public class GetEnvironmentParameterOptions {
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
        void returnsTheCorrectParameterPathBasedOnEnvironment() {
            String environment  = "qa"
            String expectedPath = "somePath"
            ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())
            doReturn(expectedPath).when(plugin).pathForEnvironment(environment)

            Map actual = plugin.getEnvironmentParameterOptions(environment)

            assertEquals(expectedPath, actual.path)
        }

        @Test
        void returnsTheCorrectCredentialsIdBasedOnEnvironment() {
            String environment           = "qa"
            String path                  = "somePath"
            String expectedCredentialsId = "${environment.toUpperCase()}_PARAMETER_STORE_ACCESS"

            ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())
            doReturn(path).when(plugin).pathForEnvironment(environment)

            Map actual = plugin.getEnvironmentParameterOptions(environment)

            assertEquals(expectedCredentialsId, actual.credentialsId.toString())
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
        void addGlobalParameterWithNoOptions() {
            String path = '/path/'
            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter(path)
            println(result)
            assertEquals([[path: '/path/']], result.globalParameterOptions)
        }

        @Test
        void addGlobalParameterWithEmptyOptions() {
            Map options = [:]
            String path = '/path/'
            ArrayList<Map> expected = []
            expected << [path: path] + options

            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter(path, options)

            assertEquals(expected, result.globalParameterOptions)
        }

        @Test
        void addGlobalParameterWithOptions() {
            Map options = [recursive: true, basename: 'relative']
            String path = '/path/'
            ArrayList<Map> expected = []
            expected << [path: path] + options

            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter(path, options)

            assertEquals(expected, result.globalParameterOptions)
        }

        @Test
        void addMulitpleGlobalParameters() {
            ArrayList<Map> expected = []
            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter('/path/')
                                                         .withGlobalParameter('/path2/', [:])
                                                         .withGlobalParameter('/path3/', [recursive: true])
                                                         .withGlobalParameter('/path4/', [basename: 'something'])
            expected << [path:'/path/']
            expected << [path:'/path2/']
            expected << [path: '/path3/', recursive: true]
            expected << [path: '/path4/', basename: 'something']
            assertEquals(expected, result.globalParameterOptions)
        }
    }
}

