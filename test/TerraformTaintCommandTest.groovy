import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformTaintCommandTest {
    @Nested
    public class WithResource {
        @Test
        void defaultsToEmpty() {
            def command = new TerraformTaintCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, equalTo("echo \"No resource set, skipping 'terraform taint'."))
        }

        @Test
        void addsResourceWhenSet() {
            def command = new TerraformTaintCommand().withResource("foo")

            def actualCommand = command.toString()
            assertThat(actualCommand, equalTo("terraform taint foo"))
        }
    }

    @Nested
    public class Plugins {
        @Test
        void areAppliedToTheCommand() {
            TerraformTaintCommandPlugin plugin = mock(TerraformTaintCommandPlugin.class)
            TerraformTaintCommand.addPlugin(plugin)

            TerraformTaintCommand command = new TerraformTaintCommand()
            command.environment = "env"
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformTaintCommandPlugin plugin = mock(TerraformTaintCommandPlugin.class)
            TerraformTaintCommand.addPlugin(plugin)

            TerraformTaintCommand command = new TerraformTaintCommand()
            command.environment = "env"

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformTaintCommandPlugin firstPlugin = mock(TerraformTaintCommandPlugin.class)
            TerraformTaintCommandPlugin secondPlugin = mock(TerraformTaintCommandPlugin.class)

            TerraformTaintCommand.addPlugin(firstPlugin)
            TerraformTaintCommand command = new TerraformTaintCommand()
            command.environment = "env"

            TerraformTaintCommand.addPlugin(secondPlugin)

            command.toString()

            verify(firstPlugin, times(1)).apply(command)
            verify(secondPlugin, times(1)).apply(command)
        }
    }
}
