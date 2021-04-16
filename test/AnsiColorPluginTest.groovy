import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class AnsiColorPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            AnsiColorPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(AnsiColorPlugin.class)))
        }
    }

    @Nested
    public class Apply {
        @Test
        void decoratesThePlanStep() {
            def stage = mock(TerraformEnvironmentStage.class)
            def plugin = spy(new AnsiColorPlugin())
            def expectedClosure = { -> }
            doReturn(expectedClosure).when(plugin).addColor()

            plugin.apply(stage)

            verify(stage).decorate(TerraformEnvironmentStage.PLAN, expectedClosure)
        }

        @Test
        void decoratesTheApplyStep() {
            def stage = mock(TerraformEnvironmentStage.class)
            def plugin = spy(new AnsiColorPlugin())
            def expectedClosure = { -> }
            doReturn(expectedClosure).when(plugin).addColor()

            plugin.apply(stage)

            verify(stage).decorate(TerraformEnvironmentStage.APPLY, expectedClosure)
        }
    }
}

