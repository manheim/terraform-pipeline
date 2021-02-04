import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuildWithParametersPluginTest {
    @BeforeEach
    @AfterEach
    void reset() {
        BuildWithParametersPlugin.reset()
    }

    private createJenkinsfileSpy() {
        def dummyJenkinsfile = spy(new DummyJenkinsfile())
        dummyJenkinsfile.docker = dummyJenkinsfile

        return dummyJenkinsfile
    }

    @Nested
    public class Init {
        @AfterEach
        void resetPlugins() {
            BuildStage.reset()
            TerraformValidateStage.reset()
            TerraformEnvironmentStage.reset()
            RegressionStage.resetPlugins()
        }

        @Test
        void modifiesBuildStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = BuildStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }

        @Test
        void modifiesTerraformValidateStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }

        @Test
        void modifiesRegressionStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = RegressionStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }
    }

    @Nested
    class Apply {
        @Test
        void worksForBuildStage() {
            def expectedClosure = { -> }
            def plugin = spy(new BuildWithParametersPlugin())
            doReturn(expectedClosure).when(plugin).addParameterToFirstStageOnly()
            def stage = mock(BuildStage.class)

            plugin.apply(stage)

            verify(stage, times(1)).decorate(expectedClosure)
        }

        @Test
        void worksForTerraformValidateStage() {
            def expectedClosure = { -> }
            def plugin = spy(new BuildWithParametersPlugin())
            doReturn(expectedClosure).when(plugin).addParameterToFirstStageOnly()
            def stage = mock(TerraformValidateStage.class)

            plugin.apply(stage)

            verify(stage, times(1)).decorate(expectedClosure)
        }

        @Test
        void worksForTerraformEnvironmentStage() {
            def expectedClosure = { -> }
            def plugin = spy(new BuildWithParametersPlugin())
            doReturn(expectedClosure).when(plugin).addParameterToFirstStageOnly()
            def stage = mock(TerraformEnvironmentStage.class)

            plugin.apply(stage)

            verify(stage, times(1)).decorate(expectedClosure)
        }

        @Test
        void worksForRegressionStage() {
            def expectedClosure = { -> }
            def plugin = spy(new BuildWithParametersPlugin())
            doReturn(expectedClosure).when(plugin).addParameterToFirstStageOnly()
            def stage = mock(RegressionStage.class)

            plugin.apply(stage)

            verify(stage, times(1)).decorate(expectedClosure)
        }
    }

    @Nested
    class AddParameterToFirstStageOnly {
        @Nested
        class WithNoParameters {
            @Test
            void runsTheInnerClosure() {
                def innerClosure = spy { -> }
                def plugin = new BuildWithParametersPlugin()
                def decoration = plugin.addParameterToFirstStageOnly()

                decoration.call(innerClosure)

                verify(innerClosure, times(1)).call()
            }

            @Test
            void doesNotAddAnyParameters() {
                def original = spy(new DummyJenkinsfile())
                def plugin = new BuildWithParametersPlugin()
                def decoration = plugin.addParameterToFirstStageOnly()

                decoration.delegate = original
                decoration.call { -> }

                verify(original, times(0)).properties(any(Object.class))
            }
        }

        @Nested
        class WithParameters {
            @Test
            void runsTheInnerClosure() {
                def original = spy(new DummyJenkinsfile())
                def innerClosure = spy { -> }
                def plugin = spy(new BuildWithParametersPlugin())
                doReturn(true).when(plugin).hasParameters()
                def decoration = plugin.addParameterToFirstStageOnly()

                decoration.delegate = original
                decoration.call(innerClosure)

                verify(innerClosure, times(1)).call()
            }

            @Test
            void addsTheParameters() {
                def expectedParameters = ['myparams']
                def original = spy(new DummyJenkinsfile())
                def plugin = spy(new BuildWithParametersPlugin())
                doReturn(true).when(plugin).hasParameters()
                doReturn(expectedParameters).when(plugin).getBuildParameters()
                def decoration = plugin.addParameterToFirstStageOnly()

                decoration.delegate = original
                decoration.call { -> }

                verify(original, times(1)).parameters(expectedParameters)
                verify(original, times(1)).properties(any(List.class))
            }

            @Test
            void addParametersOnlyOnceAfterMultipleCalls() {
                def original = spy(new DummyJenkinsfile())
                def plugin = spy(new BuildWithParametersPlugin())
                doReturn(true).when(plugin).hasParameters()
                doReturn(['myparams']).when(plugin).getBuildParameters()
                def decoration = plugin.addParameterToFirstStageOnly()

                decoration.delegate = original
                decoration.call { -> }
                decoration.call { -> }

                verify(original, times(1)).parameters(any(List.class))
                verify(original, times(1)).properties(any(List.class))
            }
        }
    }

    @Nested
    class HasParameters {
        @BeforeEach
        @AfterEach
        void reset() {
            BuildWithParametersPlugin.reset()
        }

        @Test
        void returnsFalseByDefault() {
            def plugin = new BuildWithParametersPlugin()

            assertFalse(plugin.hasParameters())
        }

        @Test
        void returnsTrueAfterABooleanParameterIsAdded() {
            BuildWithParametersPlugin.withBooleanParameter([
                name: 'MY_BOOLEAN_PARAM',
                description: 'Some true-or-false',
                defaultValue: false
            ])
            def plugin = new BuildWithParametersPlugin()

            assertTrue(plugin.hasParameters())
        }
    }

    @Nested
    class WithBooleanParameter {
        @Test
        void usesTheGivenName() {
            def expectedName = 'someBooleanName'
            BuildWithParametersPlugin.withBooleanParameter([name: expectedName])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedName, parameters['name'])
        }

        @Test
        void usesTheGivenDefaultValue() {
            def expectedDefaultValue = true
            BuildWithParametersPlugin.withBooleanParameter([defaultValue: expectedDefaultValue])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedDefaultValue, parameters['defaultValue'])
        }

        @Test
        void defaultsTheParameterClassToBoolean() {
            def expectedClass = 'hudson.model.BooleanParameterDefinition'
            BuildWithParametersPlugin.withBooleanParameter([:])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedClass, parameters['$class'])
        }

        @Test
        void defaultsToFalse() {
            def expectedDefault = false
            BuildWithParametersPlugin.withBooleanParameter([:])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedDefault, parameters['defaultValue'])
        }
    }

    @Nested
    class WithStringParameter {
        @Test
        void usesTheGivenName() {
            def expectedName = 'someStringName'
            BuildWithParametersPlugin.withStringParameter([name: expectedName])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedName, parameters['name'])
        }

        @Test
        void usesTheGivenDefaultValue() {
            def expectedDefaultValue = 'newDefault'
            BuildWithParametersPlugin.withStringParameter([defaultValue: expectedDefaultValue])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedDefaultValue, parameters['defaultValue'])
        }

        @Test
        void defaultsTheParameterClassToString() {
            def expectedClass = 'hudson.model.StringParameterDefinition'
            BuildWithParametersPlugin.withStringParameter([:])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedClass, parameters['$class'])
        }

        @Test
        void defaultsToBlank() {
            def expectedDefault = ''
            BuildWithParametersPlugin.withStringParameter([:])
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()[0]

            assertEquals(expectedDefault, parameters['defaultValue'])
        }

    }

    @Nested
    class WithParameter {
        @Test
        void addsTheGivenParameter() {
            def expectedParameter = [
                $class: 'hudson.model.StringParameterDefinition',
                name: 'someName',
                description: 'some description',
                defaultValue: ''
            ]
            BuildWithParametersPlugin.withParameter(expectedParameter)
            def plugin = new BuildWithParametersPlugin()

            def parameters = plugin.getBuildParameters()

            assertThat(parameters, contains(expectedParameter))
        }
    }
}

