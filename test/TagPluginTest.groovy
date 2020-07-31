import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
        TagPlugin.reset()
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

            assertThat(result, containsString("-var=\'tags=${expectedTags}\'"))
        }

        class WithVariableName {
            @Test
            void overridesTheDefaultVariableName() {
                def expectedVariableName = 'myVar'
                TagPlugin.withVariableName(expectedVariableName)
                def command = new TerraformPlanCommand()
                def plugin = new TagPlugin()

                plugin.apply(command)
                def result = command.toString()

                assertThat(result, containsString("-var=\'${expectedVariableName}={}'"))
            }
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

            assertThat(result, containsString("-var=\'tags=${expectedTags}\'"))
        }

        class WithVariableName {
            @Test
            void overridesTheDefaultVariableName() {
                def expectedVariableName = 'myVar'
                TagPlugin.withVariableName(expectedVariableName)
                def command = new TerraformApplyCommand()
                def plugin = new TagPlugin()

                plugin.apply(command)
                def result = command.toString()

                assertThat(result, containsString("-var=\'${expectedVariableName}={}'"))
            }
        }

    }

    public class GetTagsAsString {
        @Test
        void returnsAndEmptyMapStringIfNoKeyValuePairsWereAdded() {
            def plugin = new TagPlugin()

            def result = plugin.getTagsAsString()

            assertEquals("{}", result)
        }

        @Test
        void constructMapStringUsingASingleKeyValuePair() {
            def plugin = new TagPlugin()
            plugin.withTag('key', 'value')

            def result = plugin.getTagsAsString()

            assertEquals('{"key":"value"}', result)
        }

        @Test
        void constructMapStringUsingMultipleKeyValuePairs() {
            def plugin = new TagPlugin()
            plugin.withTag('key1', 'value1')
            plugin.withTag('key2', 'value2')

            def result = plugin.getTagsAsString()

            assertEquals('{"key1":"value1","key2":"value2"}', result)
        }

        @Test
        void constructsEnvironmentTagUsingTheGivenCommandsEnvironment() {
            def plugin = new TagPlugin()
            plugin.withEnvironmentTag()
            def command = mock(TerraformCommand.class)
            doReturn('myenv').when(command).getEnvironment()

            def result = plugin.getTagsAsString(command)

            assertEquals('{"environment":"myenv"}', result)
        }

        @Test
        void constructsEnvironmentTagUsingTheGivenTagKey() {
            def expectedTagKey = 'myEnvKey'
            def plugin = new TagPlugin()
            plugin.withEnvironmentTag(expectedTagKey)
            def command = mock(TerraformCommand.class)
            doReturn('myenv').when(command).getEnvironment()

            def result = plugin.getTagsAsString(command)

            assertEquals("{\"${expectedTagKey}\":\"myenv\"}".toString(), result)
        }

        @Test
        void preservesTheOrderOfTags() {
            TagPlugin.withTag('key1', 'value1')
                     .withEnvironmentTag()
                     .withTag('key2', 'value2')
            def plugin = new TagPlugin()
            def command = mock(TerraformCommand.class)
            doReturn('myenv').when(command).getEnvironment()

            def result = plugin.getTagsAsString(command)

            assertEquals('{"key1":"value1","environment":"myenv","key2":"value2"}', result)
        }
    }
}
