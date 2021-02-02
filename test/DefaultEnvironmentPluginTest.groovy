import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DefaultEnvironmentPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStageByDefault() {
            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()

            assertThat(actualPlugins, hasItem(instanceOf(DefaultEnvironmentPlugin.class)))
        }
    }
}

