import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class ConfirmApplyPluginTest {
    @After
    void reset() {
        ConfirmApplyPlugin.reset()
    }

    @Test
    void modifiesTerraformEnvironmentStageByDefault() {
        Collection actualPlugins = TerraformEnvironmentStage.getPlugins()

        assertThat(actualPlugins, hasItem(instanceOf(ConfirmApplyPlugin.class)))
    }

    @Test
    void ConfirmApplyPluginEnabled() {
        ConfirmApplyPlugin.enable()

        assertTrue(ConfirmApplyPlugin.enabled)
    }

    @Test
    void ConfirmApplyPluginDisabled() {
        ConfirmApplyPlugin.disable()

        assertFalse(ConfirmApplyPlugin.enabled)
    }

    class GetInputOptions {
        @Test
        void defaultsToNoExtraParameters() {
            def plugin = new ConfirmApplyPlugin()

            def parameters = plugin.getInputOptions()['parameters']

            assertNull(parameters)
        }

        @Test
        void addsTheParameterToConfirmationInputOptions() {
            def expectedParameter = ['someParameterKey': 'someParameterValue']
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withParameter(expectedParameter)

            def parameters = plugin.getInputOptions()['parameters']

            assertThat(parameters, contains(expectedParameter))
        }
    }
}

