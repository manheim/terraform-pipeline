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
            ParameterStoreBuildWrapperPlugin.reset()
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
            void doesNotDecorateTheTerraformEnvironmentStageIfNoOptionsSet() {
                def expectedClosure                     = { -> }
                String environment                      = "MyEnv"
                List options                  = []
                TerraformEnvironmentStage stage         = mock(TerraformEnvironmentStage.class)
                ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

                doReturn(environment).when(stage).getEnvironment()
                doReturn(options).when(plugin).getParameterOptions(environment)

                plugin.apply(stage)

                verify(stage, never()).decorate(expectedClosure)
            }

            @Test
            void decorateTheTerraformEnvironmentStageWhenSingleOptionsSet() {
                def expectedClosure                     = { -> }
                String environment                      = "MyEnv"
                List options                            = [[someKey: "someValue"]]
                TerraformEnvironmentStage stage         = mock(TerraformEnvironmentStage.class)
                ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

                doReturn(environment).when(stage).getEnvironment()
                doReturn(options).when(plugin).getParameterOptions(environment)
                doReturn(expectedClosure).when(plugin).addParameterStoreBuildWrapper(options[0])

                plugin.apply(stage)

                verify(stage).decorate(TerraformEnvironmentStage.PLAN, expectedClosure)
                verify(stage).decorate(TerraformEnvironmentStage.APPLY, expectedClosure)
            }

            @Test
            void decorateTheTerraformEnvironmentStageWhenMultipleOptionsSet() {
                def firstClosure                        = { -> }
                def secondClosure                       = { -> }
                String environment                      = "MyEnv"
                List options                            = [[someKey: "someValue"], [someOtherKey: "someOtherValue"]]
                TerraformEnvironmentStage stage         = mock(TerraformEnvironmentStage.class)
                ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

                doReturn(environment).when(stage).getEnvironment()
                doReturn(options).when(plugin).getParameterOptions(environment)
                doReturn(firstClosure).when(plugin).addParameterStoreBuildWrapper(options[0])
                doReturn(secondClosure).when(plugin).addParameterStoreBuildWrapper(options[1])

                plugin.apply(stage)

                verify(stage, times(2)).decorate(anyString(), eq(firstClosure))
                verify(stage, times(2)).decorate(anyString(), eq(secondClosure))
            }
        }
    }

    class GetParameterOptions {
        @After
        public void reset() {
            ParameterStoreBuildWrapperPlugin.reset()
        }

        @Test
        void returnsEnvironmentOptionWhenSet() {
            String environment                      = "MyEnv"
            Map option                              = [ key: "value" ]
            List expected                           = [ option ]
            ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

            doReturn(option).when(plugin).getEnvironmentParameterOptions(environment)

            List actual = plugin.getParameterOptions(environment)

            assertEquals(expected, actual)
        }

        @Test
        void returnsSingleGlobalOptionsAndEnvironmentOptionWhenSet() {
            String environment                      = "MyEnv"
            Map environmentOption                   = [ env: "envValue" ]
            Map globalOption1                       = [ global: "globalValue" ]
            List globalOptions                      = [ globalOption1 ]
            List expected                           = [ environmentOption ] + globalOptions
            ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

            doReturn(environmentOption).when(plugin).getEnvironmentParameterOptions(environment)
            doReturn(globalOptions).when(plugin).getGlobalParameterOptions()

            List actual = plugin.getParameterOptions(environment)

            assertEquals(expected, actual)
        }

        @Test
        void returnsMultipleGlobalOptionsAndEnvironmentOptionWhenSet() {
            String environment                      = "MyEnv"
            Map environmentOption                   = [ env: "envValue" ]
            Map globalOption1                       = [ global: "globalValue" ]
            Map globalOption2                       = [ global2: "globalValue2" ]
            List globalOptions                      = [ globalOption1, globalOption2 ]
            List expected                           = [ environmentOption ] + globalOptions
            ParameterStoreBuildWrapperPlugin plugin = spy(new ParameterStoreBuildWrapperPlugin())

            doReturn(environmentOption).when(plugin).getEnvironmentParameterOptions(environment)
            doReturn(globalOptions).when(plugin).getGlobalParameterOptions()

            List actual = plugin.getParameterOptions(environment)

            assertEquals(expected, actual)
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
            Map options   = [:]
            String path   = '/path/'
            List expected = [[path: path] + options]

            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter(path, options)

            assertEquals(expected, result.globalParameterOptions)
        }

        @Test
        void addGlobalParameterWithOptions() {
            Map options   = [recursive: true, basename: 'relative']
            String path   = '/path/'
            List expected = [[path: path] + options]

            def result = ParameterStoreBuildWrapperPlugin.withGlobalParameter(path, options)

            assertEquals(expected, result.globalParameterOptions)
        }

        @Test
        void addMulitpleGlobalParameters() {
            List expected = []
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

