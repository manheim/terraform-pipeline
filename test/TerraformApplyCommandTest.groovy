import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;


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

