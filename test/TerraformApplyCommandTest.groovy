import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.startsWith
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformApplyCommandTest {
    public class WithInput {
        @Test
        void defaultsToFalse() {
            def command = new TerraformApplyCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -input=false"))
        }

        @Test
        void setsInputFlagToFalseWhenFalse() {
            def command = new TerraformApplyCommand().withInput(false)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -input=false"))
        }

        @Test
        void skipsInputFlagWhenTrue() {
            def command = new TerraformApplyCommand().withInput(true)

            def actualCommand = command.toString()
            assertThat(actualCommand, not(containsString(" -input=false")))
        }
    }

    public class WithArgument {
        @Test
        void addsArgument() {
            def command = new TerraformApplyCommand().withArgument('foo')

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" foo"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformApplyCommand().withArgument('foo').withArgument('bar')

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
            def command = new TerraformApplyCommand().withVariable(expectedKey, expectedValue)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("-var '${expectedKey}=${expectedValue}'"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformApplyCommand().withVariable('key1', 'val1')
                                                    .withVariable('key2', 'val2')

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("-var 'key1=val1'"))
            assertThat(actualCommand, containsString("-var 'key2=val2'"))
        }

        class WithVariablePattern {
            @Test
            void usesTheNewPattern() {
                def command = new TerraformApplyCommand().withVariablePattern { key, value -> "boop-${key}-${value}-boop" }
                                                         .withVariable('foo', 'bar')

                def actualCommand = command.toString()
                assertThat(actualCommand, containsString("boop-foo-bar-boop"))
            }
        }
    }

    public class WithDirectory {
        @Test
        void addsDirectoryArgument() {
            def command = new TerraformApplyCommand().withDirectory("foobar")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith(" foobar"))
        }
    }

    public class WithPrefix {
        @Test
        void addsPrefixToBeginningOfCommand() {
            def command = new TerraformApplyCommand().withPrefix("somePrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("somePrefix"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformApplyCommand().withPrefix("fooPrefix")
                                                     .withPrefix("barPrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("fooPrefix barPrefix"))
        }
    }

    public class WithSuffix {
        @Test
        void addsSuffixToEndOfCommand() {
            def command = new TerraformApplyCommand().withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("> /dev/null"))
        }

        @Test
        void addsSuffixAfterArgumentsAndDirectories() {
            def command = new TerraformApplyCommand().withArgument('fakeArg')
                                                     .withDirectory('fakeDirectory')
                                                     .withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("> /dev/null"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformApplyCommand().withSuffix("fooSuffix")
                                                     .withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("fooSuffix > /dev/null"))
        }
    }

    public class Plugins {
        @After
        void resetPlugins() {
            TerraformApplyCommand.resetPlugins()
        }

        @Test
        void areAppliedToTheCommand() {
            TerraformApplyCommandPlugin plugin = mock(TerraformApplyCommandPlugin.class)
            TerraformApplyCommand.addPlugin(plugin)

            TerraformApplyCommand command = TerraformApplyCommand.instanceFor("env")
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformApplyCommandPlugin plugin = mock(TerraformApplyCommandPlugin.class)
            TerraformApplyCommand.addPlugin(plugin)

            TerraformApplyCommand command = TerraformApplyCommand.instanceFor("env")

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformApplyCommandPlugin firstPlugin = mock(TerraformApplyCommandPlugin.class)
            TerraformApplyCommandPlugin secondPlugin = mock(TerraformApplyCommandPlugin.class)

            TerraformApplyCommand.addPlugin(firstPlugin)
            TerraformApplyCommand command = TerraformApplyCommand.instanceFor("env")

            TerraformApplyCommand.addPlugin(secondPlugin)

            command.toString()

            verify(secondPlugin, times(1)).apply(command)
        }
    }
}

