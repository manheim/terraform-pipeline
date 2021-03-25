import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.blankString
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformImportCommandTest {
    @Nested
    public class WithResource {
        @Test
        void defaultsToEmpty() {
            def command = new TerraformImportCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("No resource set, skipping 'terraform import'."))
        }

        @Test
        void doesntRunWithoutTargetPath() {
            def command = new TerraformImportCommand().withResource("foo")

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("No target path set, skipping 'terraform import'."))
        }

        @Test
        void runsCommandWhenTargetIsAlsoSet() {
            def command = new TerraformImportCommand().withResource("foo").withTargetPath("target.foo")

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("terraform import target.foo foo"))
        }
    }

    @Nested
    public class WithTargetPath {
        @Test
        void defaultsToEmpty() {
            def command = new TerraformImportCommand()

            assertThat(command.targetPath, blankString())
        }

        @Test
        void doesntRunWithoutResource() {
            def command = new TerraformImportCommand().withTargetPath("target.foo")

            def actualCommand = command.toString()
            assertThat(actualCommand, containsString("No resource set, skipping 'terraform import'."))
        }
    }

    @Nested
    public class Plugins {
        @Test
        void areAppliedToTheCommand() {
            TerraformImportCommandPlugin plugin = mock(TerraformImportCommandPlugin.class)
            TerraformImportCommand.addPlugin(plugin)

            TerraformImportCommand command = TerraformImportCommand.instanceFor("env")
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformImportCommandPlugin plugin = mock(TerraformImportCommandPlugin.class)
            TerraformImportCommand.addPlugin(plugin)

            TerraformImportCommand command = TerraformImportCommand.instanceFor("env")

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformImportCommandPlugin firstPlugin = mock(TerraformImportCommandPlugin.class)
            TerraformImportCommandPlugin secondPlugin = mock(TerraformImportCommandPlugin.class)

            TerraformImportCommand.addPlugin(firstPlugin)
            TerraformImportCommand command = TerraformImportCommand.instanceFor("env")

            TerraformImportCommand.addPlugin(secondPlugin)

            command.toString()

            verify(secondPlugin, times(1)).apply(command)
        }
    }
}
