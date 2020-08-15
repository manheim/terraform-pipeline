import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.startsWith
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformPlanCommandTest {
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

    public class WithDirectory {
        @Test
        void addsDirectoryArgument() {
            def command = new TerraformPlanCommand().withDirectory("foobar")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith(" foobar"))
        }
    }

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

    public class WithVariable {
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

            assertTrue(expectedCommand == actualCommand)
        }
    }

    public class Plugins {
        @After
        void resetPlugins() {
            TerraformPlanCommand.resetPlugins()
        }

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

