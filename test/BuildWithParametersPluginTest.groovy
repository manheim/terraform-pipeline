import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class BuildWithParametersPluginTest {
    @Before
    @After
    void reset() {
        BuildWithParametersPlugin.reset()
    }

    private createJenkinsfileSpy() {
        def dummyJenkinsfile = spy(new DummyJenkinsfile())
        dummyJenkinsfile.docker = dummyJenkinsfile

        return dummyJenkinsfile
    }

    public class Init {
        @After
        void resetPlugins() {
            BuildStage.resetPlugins()
            TerraformValidateStage.resetPlugins()
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

    class AddParameterToFirstStageOnly {
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

        class WithParameters {
            @Test
            void runsTheInnerClosure() {
                def original = spy(new DummyJenkinsfile())
                def innerClosure = spy { -> }
                def plugin = spy(new BuildWithParametersPlugin())
                doReturn(true).when(plugin).hasParameters()
                doReturn(['some params']).when(plugin).getParameters()
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
                doReturn(expectedParameters).when(plugin).getParameters()
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
                doReturn(['myparams']).when(plugin).getParameters()
                def decoration = plugin.addParameterToFirstStageOnly()

                decoration.delegate = original
                decoration.call { -> }
                decoration.call { -> }

                verify(original, times(1)).parameters(any(List.class))
                verify(original, times(1)).properties(any(List.class))
            }
        }
    }

    class HasParameters {
        @Before
        @After
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

    class WithBooleanParameter {
        @Test(expected = RuntimeException.class)
        void raisesErrorIfMissingName() {
            BuildWithParametersPlugin.withBooleanParameter([
                description: 'Some true-or-false',
                defaultValue: false
            ])
        }

        @Test(expected = RuntimeException.class)
        void raisesErrorIfMissingDescription() {
            BuildWithParametersPlugin.withBooleanParameter([
                name: 'SomeName',
                defaultValue: false
            ])
        }
    }

    /*
    class WithStringParameter {
    }

    class WithParameter {
    }
    */
}

