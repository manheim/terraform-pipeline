import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class FormatPluginTest {
    public class Init {
        @Test
        void modifiesTerraformValidateStage() {
            FormatPlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(FormatPlugin.class)))
        }
    }

    public class ApplyForValidateStage {
        @Test
        void addsClosureToRunTerraformFormat() {
            def expectedClosure = { -> }
            def validateStage = spy(new TerraformValidateStage())
            def plugin = spy(new FormatPlugin())
            doReturn(expectedClosure).when(plugin).formatClosure()

            plugin.apply(validateStage)

            verify(validateStage).decorate(TerraformValidateStage.VALIDATE, expectedClosure)
        }
    }

    public class FormatClosure {
        @Test
        void runsTheGivenInnerClosure() {
            def wasRun = false
            def innerClosure = { -> wasRun = true }
            def dummyJenkinsfile = spy(new DummyJenkinsfile())
            def plugin = new FormatPlugin()

            def formatClosure = plugin.formatClosure()
            formatClosure.delegate = dummyJenkinsfile
            formatClosure.call(innerClosure)

            assertTrue(wasRun)
        }

        @Test
        void runsTerraformFormatCommandInAShell() {
            def expectedFormatCommand = 'terraform fmt -check'
            def dummyJenkinsfile = spy(new DummyJenkinsfile())
            def plugin = new FormatPlugin()

            def formatClosure = plugin.formatClosure()
            formatClosure.delegate = dummyJenkinsfile
            formatClosure.call { -> }

            verify(dummyJenkinsfile).sh(expectedFormatCommand)
        }
    }
}

