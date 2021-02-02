import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AnsiColorPluginTest {
    @Nested
    public class Init {
        @AfterEach
        void resetPlugins() {
            TerraformEnvironmentStage.reset()
        }

        @Test
        void modifiesTerraformEnvironmentStage() {
            AnsiColorPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(AnsiColorPlugin.class)))
        }
    }
}

