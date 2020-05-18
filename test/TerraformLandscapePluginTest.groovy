import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock;

import org.junit.Test
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformLandscapePluginTest {
    @Before
    public void resetJenkinsEnv() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
    }

    public class Init {
        @After
        void resetPlugins() {
            TerraformPlanCommand.resetPlugins()
        }

        @Test
        void modifiesTerraformPlanCommand() {
            TerraformLandscapePlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformLandscapePlugin.class)))
        }
    }

    public class Apply {

        @Test
        void addsLandscapeSuffixToTerraformPlan() {
            TerraformLandscapePlugin plugin = new TerraformLandscapePlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(" | landscape"))
        }
    }
}

