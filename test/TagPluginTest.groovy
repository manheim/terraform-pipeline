import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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

    public class ApplyForPlanCommand {
        @Test
        public void addsTheTagArgument() {
            def expectedTags = '{"key1":"value1","key2":"value2"}'
            def command = new TerraformPlanCommand()
            def plugin = spy(new TagPlugin())
            doReturn(expectedTags).when(plugin).getTagsAsString()

            plugin.apply(command)
            def result = command.toString()

            assertThat(result, containsString("-var=\'${expectedTags}\'"))
        }
    }

    public class ApplyForApplyCommand {
        @Test
        public void addsTheTagArgument() {
            def expectedTags = '{"key1":"value1","key2":"value2"}'
            def command = new TerraformApplyCommand()
            def plugin = spy(new TagPlugin())
            doReturn(expectedTags).when(plugin).getTagsAsString()

            plugin.apply(command)
            def result = command.toString()

            assertThat(result, containsString("-var=\'${expectedTags}\'"))
        }
    }

    public class GetTagsAsString {
        @Test
        void constructsArgumentsFromTheKeyValuePairs() {
            // Start testing this
            assertTrue(true)
        }
    }
}
