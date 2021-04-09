import static org.hamcrest.Matchers.equalTo
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

    @Nested
    public class ConvertOutputToEnvironmentVariable {
        @Test
        void isFluent() {
            def result = FlywayMigrationPlugin.convertOutputToEnvironmentVariable('output', 'VARIABLE')

            assertThat(result, equalTo(FlywayMigrationPlugin.class))
        }
    }

    @Nested
    public class WithPasswordFromEnvironmentVariable {
        @Test
        void isFluent() {
            def result = FlywayMigrationPlugin.withPasswordFromEnvironmentVariable('MY_PASSWORD')

            assertThat(result, equalTo(FlywayMigrationPlugin.class))
        }
    }

    @Nested
    public class WithUserFromEnvironmentVariable {
        @Test
        void isFluent() {
            def result = FlywayMigrationPlugin.withUserFromEnvironmentVariable('MY_USER')

            assertThat(result, equalTo(FlywayMigrationPlugin.class))
        }
    }

    @Nested
    public class FlywayInfoClosure {
        @Test
        void runsTheNestedClosure() {
            def plugin = new FlywayMigrationPlugin()
            def iWasCalled = false
            def nestedClosure = { -> iWasCalled = true }

            def flywayClosure = plugin.flywayInfoClosure()
            flywayClosure.delegate = new MockWorkflowScript()
            flywayClosure(nestedClosure)

            assertThat(iWasCalled, equalTo(true))
        }
    }
}

