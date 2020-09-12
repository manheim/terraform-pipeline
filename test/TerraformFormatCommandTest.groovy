import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.startsWith

import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformFormatCommandTest {
    public class ToString {
        @Test
        void includesTerraformFormatCommand() {
            def command = new TerraformFormatCommand()

            def actual = command.toString()

            assertThat(actual, startsWith('terraform fmt'))
        }

        @Test
        void includesCheckOptionByDefault() {
            def command = new TerraformFormatCommand()

            def actual = command.toString()

            assertThat(actual, endsWith('-check'))
        }
    }
}
