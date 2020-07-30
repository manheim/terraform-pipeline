import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.isA
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static TerraformEnvironmentStage.PLAN

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformEnvironmentStageTest {
    @After
    void resetPlugins() {
        TerraformEnvironmentStage.resetPlugins()
    }

    public class Then {

        @Test
        void nextStageisCalled() {
            def stage  = new TerraformEnvironmentStage('foo')
            def stage2 = new TerraformEnvironmentStage('foo2')

            def result = stage.then(stage2)

            assertThat(result, isA(BuildGraph.class))
        }
    }

    public class AddedPlugins {
        @Test
        void willHaveApplyCalled() {
            TerraformEnvironmentStagePlugin fakePlugin = mock(TerraformEnvironmentStagePlugin.class)
            TerraformEnvironmentStage.addPlugin(fakePlugin)

            TerraformEnvironmentStage stage = new TerraformEnvironmentStage('anyStage')
            stage.applyPlugins()

            verify(fakePlugin).apply(stage)
        }
    }

    public class WithEnv {
        @Test
        void addsAnInstanceOfEnvironmentVariablePlugin() {
            def stage = new TerraformEnvironmentStage('foo')
            stage.withEnv('localKey', 'localValue')

            def plugins = stage.getAllPlugins()

            assertThat(plugins, hasItem(isA(EnvironmentVariablePlugin.class)))
        }

        @Test
        void preservesOrderOfOtherPlugins() {
            def stage = new TerraformEnvironmentStage('foo')
            def plugin1 = mock(TerraformEnvironmentStagePlugin.class)
            def plugin3 = mock(TerraformEnvironmentStagePlugin.class)

            stage.addPlugin(plugin1)
            stage.withEnv('someKey', 'someValue')
            stage.addPlugin(plugin3)

            def plugins = stage.getAllPlugins()

            def plugin1Index = plugins.findIndexOf { it == plugin1 }

            assertThat(plugins[plugin1Index], is(plugin1))
            assertThat(plugins[plugin1Index + 1], isA(EnvironmentVariablePlugin.class))
            assertThat(plugins[plugin1Index + 2], is(plugin3))
        }

        @Test
        void doesNotAddPluginToOtherInstances() {
            def modifiedStage = new TerraformEnvironmentStage('modified')
            def unmodifiedStage = new TerraformEnvironmentStage('unmodified')

            def pluginsBefore = unmodifiedStage.getAllPlugins()

            modifiedStage.withEnv('somekey', 'somevalue')

            def pluginsAfter = unmodifiedStage.getAllPlugins()

            assertEquals(pluginsBefore, pluginsAfter)
        }

        @Test
        void isFluent() {
            def stage = new TerraformEnvironmentStage('foo')
            def result = stage.withEnv('somekey', 'somevalue')

            assertTrue(result == stage)
        }
    }

    public class WithGlobalEnv {
        @Test
        void addsAnInstanceOfEnvironmeentVariablePlugin() {
            TerraformEnvironmentStage.withGlobalEnv('globalKey', 'globalValue')

            def plugins = TerraformEnvironmentStage.getPlugins()

            assertThat(plugins, hasItem(isA(EnvironmentVariablePlugin.class)))
        }

        @Test
        void isFluent() {
            def result = TerraformEnvironmentStage.withGlobalEnv('somekey', 'somevalue')

            assertTrue(result == TerraformEnvironmentStage.class)
        }
    }

    class PipelineConfigurations {
        @Before
        void resetBefore() {
            Jenkinsfile.reset()
        }

        @After
        void resetAfter() {
            Jenkinsfile.reset()
        }

        @Test
        void returnsAClosure() {
            def stage = new TerraformEnvironmentStage('foo')

            def result = stage.pipelineConfiguration()

            assertThat(result, isA(Closure.class))
        }

        @Test
        void doesNotBlowUpWhenRunningClosure() {
            Jenkinsfile.instance = spy(new Jenkinsfile())
            doReturn([:]).when(Jenkinsfile.instance).getEnv()
            Jenkinsfile.defaultNodeName = 'foo'
            def stage = new TerraformEnvironmentStage('foo')

            def closure = stage.pipelineConfiguration()
            closure.delegate = new DummyJenkinsfile()
            closure()
        }
    }

    class WithStageNamePattern {
        @Before
        @After
        void reset() {
            TerraformEnvironmentStage.reset()
        }

        @Test
        void constructsTheDefaultStageNameWhenBlank() {
            def stage = new TerraformEnvironmentStage('myenv')

            def actualName = stage.getStageNameFor(PLAN)

            assertEquals('plan-myenv', actualName)
        }

        @Test
        void constructTheStageNameUsingTheGivenPattern() {
            TerraformEnvironmentStage.withStageNamePattern { options -> "${options['command']}-override-${options['environment']}" }
            def stage = new TerraformEnvironmentStage('myenv')

            def actualName = stage.getStageNameFor(PLAN)

            assertEquals('plan-override-myenv', actualName)
        }
    }
}
