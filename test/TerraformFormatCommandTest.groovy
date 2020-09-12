import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.startsWith

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformFormatCommandTest {
    @Before
    @After
    public void reset() {
        TerraformFormatCommand.reset()
    }

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

        public class WithRecursive {
            @Test
            void includesCheckOptionByDefault() {
                def command = new TerraformFormatCommand()

                TerraformFormatCommand.withRecursive()
                def actual = command.toString()

                assertThat(actual, containsString('-recursive'))
            }
        }
    }
}
