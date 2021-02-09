import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformLandscapePluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformPlanCommand() {
            TerraformLandscapePlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformLandscapePlugin.class)))
        }
    }

    @Nested
    public class Apply {
        @Test
        void addsLandscapeArgumentToTerraformPlan() {
            TerraformLandscapePlugin plugin = new TerraformLandscapePlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("| landscape"))
        }
    }
}
