import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.hamcrest.Matchers.*

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

