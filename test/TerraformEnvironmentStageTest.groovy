import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.isA
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformEnvironmentStageTest {
    @After
    void resetPlugins() {
        TerraformEnvironmentStage.resetPlugins()
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
}
