import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TerraformLandscapePluginTest {
    @BeforeEach
    public void resetJenkinsEnv() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
    }

    @Nested
    public class Init {
        @AfterEach
        void resetPlugins() {
            TerraformPlanCommand.reset()
        }

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
