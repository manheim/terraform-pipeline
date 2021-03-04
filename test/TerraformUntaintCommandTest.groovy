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
class TerraformUntaintCommandTest {
    @Nested
    public class WithResource {
        @Test
        void defaultsToEmpty() {
            def command = new TerraformUntaintCommand()

            def actualCommand = command.toString()
            assertThat(actualCommand, equalTo("echo \"No resource set, skipping 'terraform untaint'."))
        }

        @Test
        void addsResourceWhenSet() {
            def command = new TerraformUntaintCommand().withResource("foo")

            def actualCommand = command.toString()
            assertThat(actualCommand, equalTo("terraform untaint foo"))
        }
    }

    @Nested
    public class Plugins {
        @Test
        void areAppliedToTheCommand() {
            TerraformUntaintCommandPlugin plugin = mock(TerraformUntaintCommandPlugin.class)
            TerraformUntaintCommand.addPlugin(plugin)

            TerraformUntaintCommand command = new TerraformUntaintCommand()
            command.environment = "env"
            command.toString()

            verify(plugin).apply(command)
        }

        @Test
        void areAppliedExactlyOnce() {
            TerraformUntaintCommandPlugin plugin = mock(TerraformUntaintCommandPlugin.class)
            TerraformUntaintCommand.addPlugin(plugin)

            TerraformUntaintCommand command = new TerraformUntaintCommand()
            command.environment = "env"

            String firstCommand = command.toString()
            String secondCommand = command.toString()

            verify(plugin, times(1)).apply(command)
        }

        @Test
        void areAppliedEvenAfterCommandAlreadyInstantiated() {
            TerraformUntaintCommandPlugin firstPlugin = mock(TerraformUntaintCommandPlugin.class)
            TerraformUntaintCommandPlugin secondPlugin = mock(TerraformUntaintCommandPlugin.class)

            TerraformUntaintCommand.addPlugin(firstPlugin)
            TerraformUntaintCommand command = new TerraformUntaintCommand()
            command.environment = "env"

            TerraformUntaintCommand.addPlugin(secondPlugin)

            command.toString()

            verify(firstPlugin, times(1)).apply(command)
            verify(secondPlugin, times(1)).apply(command)
        }
    }
}
