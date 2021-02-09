import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyMap;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TagPluginTest {
    @Nested
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

    @Nested
    class WithTag {
        @Test
        void isFluent() {
            def result = TagPlugin.withTag('key', 'value')

            assertThat(result, equalTo(TagPlugin.class))
        }
    }

    @Nested
    class WithTagFromFile {
        @Test
        void isFluent() {
            def result = TagPlugin.withTagFromFile('key', 'value')

            assertThat(result, equalTo(TagPlugin.class))
        }
    }

    @Nested
    class WithTagFromEnvironmentVariable {
        @Test
        void isFluent() {
            def result = TagPlugin.withTagFromEnvironmentVariable('key', 'variable')

            assertThat(result, equalTo(TagPlugin.class))
        }
    }

    @Nested
    class WithEnvironmentTag {
        @Test
        void isFluent() {
            def result = TagPlugin.withEnvironmentTag()

            assertThat(result, equalTo(TagPlugin.class))
        }
    }

    @Nested
    public class ApplyForPlanCommand {
        @Test
        public void addsTheTagArgument() {
            def expectedVariableName = 'tags'
            def expectedTags = [key1:'value1', key2: 'value2']
            def command = spy(new TerraformPlanCommand())
            def plugin = spy(new TagPlugin())
            doReturn(expectedTags).when(plugin).getTags(command)

            plugin.apply(command)

            verify(command).withVariable(expectedVariableName, expectedTags)
        }

        @Nested
        class WithVariableName {
            @Test
            void overridesTheDefaultVariableName() {
                def expectedVariableName = 'myVar'
                TagPlugin.withVariableName(expectedVariableName)
                def command = spy(new TerraformPlanCommand())
                def plugin = spy(new TagPlugin())
                def expectedTags = [key1: 'value1', key2: 'value2']
                doReturn(expectedTags).when(plugin).getTags(command)

                plugin.apply(command)

                verify(command).withVariable(expectedVariableName, expectedTags)
            }
        }
    }

    @Nested
    public class ApplyForApplyCommand {
        @Test
        public void addsTheTagArgument() {
            def expectedVariableName = 'tags'
            def expectedTags = [key1:'value1', key2:'value2']
            def command = spy(new TerraformApplyCommand())
            def plugin = spy(new TagPlugin())
            doReturn(expectedTags).when(plugin).getTags(command)

            plugin.apply(command)

            verify(command).withVariable(expectedVariableName, expectedTags)
        }

        @Test
        public void doesNotAddVariablesWhenPluginIsDisabled() {
            def command = spy(new TerraformApplyCommand())
            def plugin = new TagPlugin()

            TagPlugin.disableOnApply()
            plugin.apply(command)

            verify(command, times(0)).withVariable(anyString(), anyMap())
        }

        @Nested
        class WithVariableName {
            @Test
            void overridesTheDefaultVariableName() {
                def expectedVariableName = 'myVar'
                def expectedTags = [key1:'value1', key2:'value2']
                TagPlugin.withVariableName(expectedVariableName)
                def command = spy(new TerraformApplyCommand())
                def plugin = spy(new TagPlugin())
                doReturn(expectedTags).when(plugin).getTags(command)

                plugin.apply(command)

                verify(command).withVariable(expectedVariableName, expectedTags)
            }
        }
    }

    @Nested
    public class GetTags {
        @Test
        void returnsAndEmptyMapIfNoKeyValuePairsWereAdded() {
            def plugin = new TagPlugin()

            def result = plugin.getTags()

            assertThat(result, equalTo([:]))
        }

        @Test
        void constructMapStringUsingASingleKeyValuePair() {
            def plugin = new TagPlugin()
            plugin.withTag('mykey', 'myvalue')

            def result = plugin.getTags()

            assertThat(result, equalTo([mykey: 'myvalue']))
        }

        @Test
        void constructMapStringUsingMultipleKeyValuePairs() {
            def plugin = new TagPlugin()
            plugin.withTag('key1', 'value1')
            plugin.withTag('key2', 'value2')

            def result = plugin.getTags()

            assertThat(result, equalTo([key1: 'value1', key2: 'value2']))
        }

        @Test
        void constructsEnvironmentTagUsingTheGivenCommandsEnvironment() {
            def plugin = new TagPlugin()
            plugin.withEnvironmentTag()
            def command = mock(TerraformCommand.class)
            doReturn('myenv').when(command).getEnvironment()

            def result = plugin.getTags(command)

            assertThat(result, equalTo([ environment: 'myenv' ]))
        }

        @Test
        void constructsEnvironmentTagUsingTheGivenTagKey() {
            def expectedTagKey = 'myEnvKey'
            def plugin = new TagPlugin()
            plugin.withEnvironmentTag(expectedTagKey)
            def command = mock(TerraformCommand.class)
            doReturn('myenv').when(command).getEnvironment()

            def result = plugin.getTags(command)

            Map expectedResult = [:]
            expectedResult[expectedTagKey] = 'myenv'
            assertThat(result, equalTo(expectedResult))
        }

        @Test
        void constructsTagsUsingTheGivenFile() {
            def key = 'change-id'
            def file = 'changeId.txt'
            def fileContent = 'someContent'
            MockJenkinsfile.withFile(file, fileContent)
            def plugin = new TagPlugin()
            plugin.withTagFromFile(key, file)

            def result = plugin.getTags(mock(TerraformCommand.class))

            assertThat(result, equalTo(['change-id': "${fileContent}".toString()]))
        }

        @Test
        void constructsTagsFromEnvironmentVariables() {
            def variable = 'MY_ENV'
            def expectedValue = 'valueOfMY_ENV'
            def plugin = new TagPlugin()
            plugin.withTagFromEnvironmentVariable('someTagName', variable)
            def command = mock(TerraformCommand.class)
            def original = new MockWorkflowScript()
            original.env = [:]
            original.env[variable] = expectedValue

            Jenkinsfile.original = original

            def result = plugin.getTags(command)

            assertThat(result, equalTo([ 'someTagName': expectedValue ]))
        }

        @Test
        void preservesTheOrderOfTags() {
            TagPlugin.withTag('key1', 'value1')
                     .withEnvironmentTag()
                     .withTag('key2', 'value2')
            def plugin = new TagPlugin()
            def command = mock(TerraformCommand.class)
            doReturn('myenv').when(command).getEnvironment()

            def result = plugin.getTags(command)

            assertThat(result, equalTo([key1: 'value1', environment: 'myenv', key2: 'value2']))
        }
    }

    @Nested
    class DisableOnApply {
        @Test
        void isFluent() {
            def result = TagPlugin.disableOnApply()

            assertThat(result, equalTo(TagPlugin.class))
        }
    }
}
