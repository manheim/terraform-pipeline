import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertFalse
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
        ConfirmApplyPlugin.enabled = true
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
}

