import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformOutputCommandTest {
    @Nested
    public class WithJson {
        @Test
        void defaultsToFalse() {
            def command = new TerraformOutputCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, not(containsString("-json")))
        }

        @Test
        void skipsJsonFlagWhenFalse() {
            def command = new TerraformOutputCommand().withJson(false)

            def actualCommand = command.toString()
            assertThat(actualCommand, not(containsString(" -json")))
        }

        @Test
        void addsJsonFlagWhenTrue() {
            def command = new TerraformOutputCommand().withJson(true)

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(" -json"))
        }
    }

    @Nested
    public class WithRedirectFile {
        @Test
        void defaultsToEmpty() {
            def command = new TerraformOutputCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, not(containsString(">")))
        }

        @Test
        void addsRedirectWhenSet() {
            def command = new TerraformOutputCommand().withRedirectFile("foo")

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString(">foo"))
        }
    }

    @Nested
    public class Plugins {
        @Test
        void areAppliedToTheCommand() {
            TerraformOutputCommandPlugin plugin = mock(TerraformOutputCommandPlugin.class)
            TerraformOutputCommand.addPlugin(plugin)

            TerraformOutputCommand command = TerraformOutputCommand.instanceFor("env")
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformOutputCommandPlugin plugin = mock(TerraformOutputCommandPlugin.class)
            TerraformOutputCommand.addPlugin(plugin)

            TerraformOutputCommand command = TerraformOutputCommand.instanceFor("env")

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformOutputCommandPlugin firstPlugin = mock(TerraformOutputCommandPlugin.class)
            TerraformOutputCommandPlugin secondPlugin = mock(TerraformOutputCommandPlugin.class)

            TerraformOutputCommand.addPlugin(firstPlugin)
            TerraformOutputCommand command = TerraformOutputCommand.instanceFor("env")

            TerraformOutputCommand.addPlugin(secondPlugin)

            command.toString()

            verify(secondPlugin, times(1)).apply(command)
        }
    }
}
