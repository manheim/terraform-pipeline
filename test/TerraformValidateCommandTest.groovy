import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.startsWith
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformValidateCommandTest {
    @Nested
    public class WithDirectory {
        @Test
        void addsDirectoryArgument() {
            def command = new TerraformValidateCommand().withDirectory("foobar")

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -chdir=foobar"))
        }
    }

    @Nested
    public class WithPrefix {
        @Test
        void addsPrefixToBeginningOfCommand() {
            def command = new TerraformValidateCommand().withPrefix("somePrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("somePrefix"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformValidateCommand().withPrefix("fooPrefix")
                                                     .withPrefix("barPrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("fooPrefix barPrefix"))
        }
    }

    @Nested
    public class WithSuffix {
        @Test
        void addsSuffixToEndOfCommand() {
            def command = new TerraformValidateCommand().withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("> /dev/null"))
        }

        @Test
        void addsSuffixAfterArgumentsAndDirectories() {
            def command = new TerraformValidateCommand().withArgument('fakeArg')
                                                        .withDirectory('fakeDirectory')
                                                        .withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("> /dev/null"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformValidateCommand().withSuffix("fooSuffix")
                                                        .withSuffix("> /dev/null")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("fooSuffix > /dev/null"))
        }
    }

    @Nested
    public class Plugins {
        @Test
        void areAppliedToTheCommand() {
            TerraformValidateCommandPlugin plugin = mock(TerraformValidateCommandPlugin.class)
            TerraformValidateCommand.addPlugin(plugin)

            TerraformValidateCommand command = TerraformValidateCommand.instance()
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformValidateCommandPlugin plugin = mock(TerraformValidateCommandPlugin.class)
            TerraformValidateCommand.addPlugin(plugin)

            TerraformValidateCommand command = TerraformValidateCommand.instance()

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformValidateCommandPlugin firstPlugin = mock(TerraformValidateCommandPlugin.class)
            TerraformValidateCommandPlugin secondPlugin = mock(TerraformValidateCommandPlugin.class)

            TerraformValidateCommand.addPlugin(firstPlugin)
            TerraformValidateCommand command = TerraformValidateCommand.instance()

            TerraformValidateCommand.addPlugin(secondPlugin)

            command.toString()

            verify(secondPlugin, times(1)).apply(command)
        }
    }
}

