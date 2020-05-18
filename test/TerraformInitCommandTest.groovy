import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.startsWith
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformInitCommandTest {
    public class WithInput {
        @Test
        void defaultsToFalse() {
            def command = new TerraformInitCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -input=false"))
        }

        @Test
        void setsInputFlagToFalseWhenFalse() {
            def command = new TerraformInitCommand().withInput(false)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -input=false"))
        }

        @Test
        void skipsInputFlagWhenTrue() {
            def command = new TerraformInitCommand().withInput(true)

            def actualCommand = command.toString()
            assertThat(actualCommand, not(containsString(" -input=false")))
        }
    }

    public class WithBackendConfig {
        @Test
        void notPresentByDefault() {
            def command = new TerraformInitCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, not(containsString(" -backend-config=")))
        }

        @Test
        void addsBackendConfigValues() {
            def command = new TerraformInitCommand().withBackendConfig("foobar")

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -backend-config=foobar"))
        }

        @Test
        void addsBackendConfigIsCumulative() {
            def command = new TerraformInitCommand().withBackendConfig("foo")
                                                    .withBackendConfig("bar")

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -backend-config=foo"))
            assertThat(actualCommand, containsString(" -backend-config=bar"))
        }

        @Test
        void withoutBackendOnlyAddsBackendFalse() {
            def command = new TerraformInitCommand().withBackendConfig("foo")
                                                    .withBackendConfig("bar")
                                                    .withoutBackend()
            def actualCommand = command.toString()

            assertThat(actualCommand, not(containsString("-backend-config=foo")))
            assertThat(actualCommand, not(containsString("-backend-config=bar")))
            assertThat(actualCommand, containsString("-backend=false"))
        }
    }

    public class WithDirectory {
        @Test
        void addsDirectoryArgument() {
            def command = new TerraformInitCommand().withDirectory("foobar")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith(" foobar"))
        }
    }

    public class WithPrefix {
        @Test
        void addsPrefixToBeginningOfCommand() {
            def command = new TerraformInitCommand().withPrefix("somePrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("somePrefix"))
        }

        @Test
        void isCumulative() {
            def command = new TerraformInitCommand().withPrefix("fooPrefix")
                                                    .withPrefix("barPrefix")

            def actualCommand = command.toString()
            assertThat(actualCommand, startsWith("fooPrefix barPrefix"))
        }
    }

    public class WithSuffix {
        @Test
        void addsSuffixToEndOfCommand() {
            def command = new TerraformInitCommand().withSuffix("\"")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("\""))
        }

        @Test
        void isCumulative() {
            def command = new TerraformInitCommand().withSuffix("fooSuffix")
                                                    .withSuffix("\"")

            def actualCommand = command.toString()
            assertThat(actualCommand, endsWith("fooSuffix \""))
        }
    }

    public class Plugins {
        @After
        void resetPlugins() {
            TerraformInitCommand.resetPlugins()
        }

        @Test
        void areAppliedToTheCommand() {
            TerraformInitCommandPlugin plugin = mock(TerraformInitCommandPlugin.class)
            TerraformInitCommand.addPlugin(plugin)

            TerraformInitCommand command = TerraformInitCommand.instanceFor("env")
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformInitCommandPlugin plugin = mock(TerraformInitCommandPlugin.class)
            TerraformInitCommand.addPlugin(plugin)

            TerraformInitCommand command = TerraformInitCommand.instanceFor("env")

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformInitCommandPlugin firstPlugin = mock(TerraformInitCommandPlugin.class)
            TerraformInitCommandPlugin secondPlugin = mock(TerraformInitCommandPlugin.class)

            TerraformInitCommand.addPlugin(firstPlugin)
            TerraformInitCommand command = TerraformInitCommand.instanceFor("env")

            TerraformInitCommand.addPlugin(secondPlugin)

            command.toString()

            verify(secondPlugin, times(1)).apply(command)
        }
    }
}

