import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.hamcrest.Matchers.*

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

