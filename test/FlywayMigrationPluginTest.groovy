import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class FlywayMigrationPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            FlywayMigrationPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(FlywayMigrationPlugin.class)))
        }
    }

    @Nested
    public class Apply {
        @Test
        void addsFlywayInfoClosureOnPlan() {
            def infoClosure = { -> }
            def plugin = spy(new FlywayMigrationPlugin())
            doReturn(infoClosure).when(plugin).flywayInfoClosure()
            def stage = mock(TerraformEnvironmentStage.class)

            plugin.apply(stage)

            verify(stage).decorate(TerraformEnvironmentStage.PLAN, infoClosure)
        }
    }
}

