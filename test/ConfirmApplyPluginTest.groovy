import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
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

            def parameters = plugin.getInputOptions('env')['parameters']

            assertNull(parameters)
        }

        @Test
        void addsTheParameterToConfirmationInputOptions() {
            def expectedParameter = ['someParameterKey': 'someParameterValue']
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withParameter(expectedParameter)

            def parameters = plugin.getInputOptions('env')['parameters']

            assertThat(parameters, contains(expectedParameter))
        }

        @Test
        void interpolatesEnvironmentVariableInConfirmMessage() {
            def environment = 'foo'
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withConfirmMessage('confirm for ${environment}')

            def message = plugin.getInputOptions(environment)['message']

            assertEquals("confirm for foo", message)
        }

        @Test
        void interpolatesEnvironmentVariableInAddParameters() {
            def environment = 'foo'
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withParameter([
                name: 'confirm',
                description: 'confirm for ${environment}'
            ])

            def description = plugin.getInputOptions(environment)['parameters'][0]['description']

            assertEquals("confirm for foo", description)
        }
    }
}

