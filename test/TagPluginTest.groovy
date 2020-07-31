import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TagPluginTest {
    @Before
    @After
    public void reset() {
        TerraformApplyCommand.resetPlugins()
        TerraformPlanCommand.resetPlugins()
    }

    public class Init {
        @Test
        void modifiesTerraformPlanCommand() {
            TagPlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TagPlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            TagPlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TagPlugin.class)))
        }
    }
}
