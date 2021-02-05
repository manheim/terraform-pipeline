import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class ValidateFormatPluginTest {
    public class Init {
        @Test
        void modifiesTerraformValidateStage() {
            ValidateFormatPlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ValidateFormatPlugin.class)))
        }

        @Test
        void enablesCheckOnTerraformFormat() {
            ValidateFormatPlugin.init()

            assertTrue(TerraformFormatCommand.isCheckEnabled())
        }
    }

    @Nested
    public class ApplyForValidateStage {
        @Test
        void addsClosureToRunTerraformFormat() {
            def expectedClosure = { -> }
            def validateStage = spy(new TerraformValidateStage())
            def plugin = spy(new ValidateFormatPlugin())
            doReturn(expectedClosure).when(plugin).formatClosure()

            plugin.apply(validateStage)

            verify(validateStage).decorate(TerraformValidateStage.VALIDATE, expectedClosure)
        }
    }

    @Nested
    public class FormatClosure {
        @Test
        void runsTheGivenInnerClosure() {
            def wasRun = false
            def innerClosure = { -> wasRun = true }
            def dummyJenkinsfile = spy(new DummyJenkinsfile())
            def plugin = new ValidateFormatPlugin()

            def formatClosure = plugin.formatClosure()
            formatClosure.delegate = dummyJenkinsfile
            formatClosure.call(innerClosure)

            assertTrue(wasRun)
        }

        @Test
        void runsTerraformFormatCommandInAShell() {
            def expectedFormatCommand = 'terraform fmt'
            def dummyJenkinsfile = spy(new DummyJenkinsfile())
            def plugin = new ValidateFormatPlugin()

            def formatClosure = plugin.formatClosure()
            formatClosure.delegate = dummyJenkinsfile
            formatClosure.call { -> }

            verify(dummyJenkinsfile).sh(expectedFormatCommand)
        }
    }
}

