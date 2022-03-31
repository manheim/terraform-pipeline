import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.startsWith
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verifyNoMoreInteractions

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformPlanCommandTest {
    @Nested
    public class WithInput {
        @Test
        void defaultsToFalse() {
            def command = new TerraformPlanCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -input=false"))
        }

        @Test
        void setsInputFlagToFalseWhenFalse() {
            def command = new TerraformPlanCommand().withInput(false)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -input=false"))
        }

        @Test
        void skipsInputFlagWhenTrue() {
            def command = new TerraformPlanCommand().withInput(true)

            def actualCommand = command.toString()
            assertThat(actualCommand, not(containsString(" -input=false")))
        }
    }

    @Nested
    public class WithDirectory {
        @Test
        void addsDirectoryArgument() {
            def command = new TerraformPlanCommand().withDirectory("foobar")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith(" foobar"))
        }

        @Test
        void addsDirectoryArgumentWithChangeDirectoryFlag() {
            def command = new TerraformPlanCommand().withDirectory("foobar")
                                                    .withChangeDirectoryFlag()

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -chdir=foobar"))
        }
    }

    @Nested
    public class WithPrefix {
        @Test
        void addsPrefixToBeginningOfCommand() {
            def command = new TerraformPlanCommand().withPrefix("somePrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("somePrefix"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformPlanCommand().withPrefix("fooPrefix")
                                                     .withPrefix("barPrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("fooPrefix barPrefix"))
        }
    }

    @Nested
    public class WithSuffix {
        @Test
        void addsSuffixToEndOfCommand() {
            def command = new TerraformPlanCommand().withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("> /dev/null"))
        }

        @Test
        void addsSuffixAfterArgumentsAndDirectories() {
            def command = new TerraformPlanCommand().withArgument('fakeArg')
                                                    .withDirectory('fakeDirectory')
                                                    .withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("> /dev/null"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformPlanCommand().withSuffix("fooSuffix")
                                                    .withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("fooSuffix > /dev/null"))
        }
    }

    @Nested
    public class WithArgument {
        @Test
        void addsArgument() {
            def command = new TerraformPlanCommand().withArgument('foo')

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" foo"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformPlanCommand().withArgument('foo').withArgument('bar')

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" foo"))
            assertThat(actualCommand, containsString(" bar"))
        }
    }

    @Nested
    public class WithVariableString {
        @Test
        void addsArgument() {
            def expectedKey = 'myKey'
            def expectedValue = 'myValue'
            def command = new TerraformPlanCommand().withVariable(expectedKey, expectedValue)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("-var '${expectedKey}=${expectedValue}'"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformPlanCommand().withVariable('key1', 'val1')
                                                    .withVariable('key2', 'val2')

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("-var 'key1=val1'"))
            assertThat(actualCommand, containsString("-var 'key2=val2'"))
        }

        class WithVariablePattern {
            @Test
            void usesTheNewPattern() {
                def command = new TerraformPlanCommand().withVariablePattern { key, value -> "boop-${key}-${value}-boop" }
                                                        .withVariable('foo', 'bar')

                def actualCommand = command.toString()
                assertThat(actualCommand, containsString("boop-foo-bar-boop"))
            }
        }
    }

    @Nested
    public class WithVariableFile {
        @Test
        void isFluent1() {
            def stage = new TerraformPlanCommand('foo')
            def result = stage.withVariableFile('somekey')

            assertThat(result, equalTo(stage))
        }

        @Test
        void testWithFile() {
            def filename = 'filename'
            def command = new TerraformPlanCommand().withVariableFile(filename)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("-var-file=./${filename}"))
        }
    }

    @Nested
    public class WithVariableFileAndMap {
        @Test
        void isFluent() {
            def stage = new TerraformPlanCommand('foo')
            Jenkinsfile.original = mock(MockWorkflowScript.class)
            def result = stage.withVariableFile('somekey', ['key':'value'])

            assertThat(result, equalTo(stage))
        }

        @Test
        void testWithFileAndMap() {
            def myKey = 'myKey'
            def expectedValue = 'expValue'
            def map = [expectedKey:expectedValue]
            def original = mock(MockWorkflowScript.class)
            Jenkinsfile.original = original
            def command = spy(new TerraformPlanCommand("dev")).withVariableFile(myKey, map)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("-var-file=./dev-${myKey}"))

            def filename = "dev-${myKey}.tfvars"
            verify(command).withVariableFile(filename)
            def content = "${myKey}={expectedKey=\"${expectedValue}\"}"
            verify(original).writeFile(file: filename.toString(), text: content.toString())
            verifyNoMoreInteractions(original)
        }
    }

    @Nested
    public class WithVariableMap {
        @Test
        void convertsMapToStringAndTreatsLikeAStringVariable() {
            def expectedKey = 'myKey'
            def expectedMap = [mapKey: 'mapValue']
            def command = spy(new TerraformPlanCommand())
            doReturn('someMapString').when(command).convertMapToCliString(expectedMap)

            command.withVariable(expectedKey, expectedMap)

            verify(command).withVariable(expectedKey, 'someMapString')
        }
    }

    @Nested
    public class ConvertMapToCliString {
        @Test
        void handlesSingleKeyPair() {
            def map = [mapKey: 'mapValue']
            def command = new TerraformPlanCommand()

            def result = command.convertMapToCliString(map)
            assertThat(result, equalTo('{mapKey=\"mapValue\"}'))
        }

        @Test
        void handlesMultipleKeyPairs() {
            def map = [mapKey1: 'mapValue1', mapKey2: 'mapValue2']
            def command = new TerraformPlanCommand()

            def result = command.convertMapToCliString(map)
            assertThat(result, equalTo('{mapKey1=\"mapValue1\",mapKey2=\"mapValue2\"}'))
        }

        @Test
        void usesMapPatternIfGiven() {
            def command = new TerraformPlanCommand().withMapPattern { map ->
                def result = map.collect { key, value -> "${value}|${key}" }.join(';')
                return "[${result}]"
            }
            def map = [mapKey1: 'mapValue1', mapKey2: 'mapValue2']

            def result = command.convertMapToCliString(map)
            assertThat(result, equalTo('[mapValue1|mapKey1;mapValue2|mapKey2]'))
        }
    }

    @Nested
    public class WithStandardErrorRedirection {
        @Test
        void sendsStandardErrorToTheGivenFile() {
            def command = new TerraformPlanCommand().withStandardErrorRedirection('error.txt')

            def actualCommand = command.toString()

            assertThat(actualCommand, containsString("2>error.txt"))
        }

        @Test
        void comesBeforeSuffix() {
            def command = new TerraformPlanCommand()
            command.withSuffix('| xargs echo')
                   .withStandardErrorRedirection('error.txt')

            def actualCommand = command.toString()

            assertThat(actualCommand, containsString("2>error.txt | xargs echo"))
        }

        @Test
        void isFluent() {
            def expectedCommand = new TerraformPlanCommand()
            def actualCommand = expectedCommand.withStandardErrorRedirection('error.txt')

            assertThat(expectedCommand, equalTo(actualCommand))
        }
    }

    @Nested
    public class Plugins {
        @Test
        void areAppliedToTheCommand() {
            TerraformPlanCommandPlugin plugin = mock(TerraformPlanCommandPlugin.class)
            TerraformPlanCommand.addPlugin(plugin)

            TerraformPlanCommand command = TerraformPlanCommand.instanceFor("env")
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformPlanCommandPlugin plugin = mock(TerraformPlanCommandPlugin.class)
            TerraformPlanCommand.addPlugin(plugin)

            TerraformPlanCommand command = TerraformPlanCommand.instanceFor("env")

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformPlanCommandPlugin firstPlugin = mock(TerraformPlanCommandPlugin.class)
            TerraformPlanCommandPlugin secondPlugin = mock(TerraformPlanCommandPlugin.class)

            TerraformPlanCommand.addPlugin(firstPlugin)
            TerraformPlanCommand command = TerraformPlanCommand.instanceFor("env")

            TerraformPlanCommand.addPlugin(secondPlugin)

            command.toString()

            verify(secondPlugin, times(1)).apply(command)
        }
    }
}

