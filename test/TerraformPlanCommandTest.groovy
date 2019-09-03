import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;


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

