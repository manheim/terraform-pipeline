import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class DefaultEnvironmentPluginTest {
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStageByDefault() {
            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()

            assertThat(actualPlugins, hasItem(instanceOf(DefaultEnvironmentPlugin.class)))
        }
    }
}

