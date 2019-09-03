import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.*

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
