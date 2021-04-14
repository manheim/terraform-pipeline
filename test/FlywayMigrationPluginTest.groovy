import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.any
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq
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

        @Test
        void addsFlywayMigrateClosureOnApply() {
            def migrateClosure = { -> }
            def plugin = spy(new FlywayMigrationPlugin())
            doReturn(migrateClosure).when(plugin).flywayMigrateClosure()
            def stage = mock(TerraformEnvironmentStage.class)

            plugin.apply(stage)

            verify(stage).decorate(TerraformEnvironmentStage.APPLY, migrateClosure)
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
    public class WithEchoEnabled {
        @Test
        void isFluent() {
            def result = FlywayMigrationPlugin.withEchoEnabled()

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

        @Test
        void setsTheListOfOptionalEnvironmentVariables() {
            def plugin = spy(new FlywayMigrationPlugin())
            def expectedList = ['KEY=value']
            doReturn(expectedList).when(plugin).buildEnvironmentVariableList(any(List.class))
            def flywayClosure = plugin.flywayInfoClosure()
            def mockWorkflowScript = spy(new MockWorkflowScript())
            flywayClosure.delegate = mockWorkflowScript

            flywayClosure { -> }

            verify(mockWorkflowScript).withEnv(eq(expectedList), any(Closure.class))
        }
    }

    @Nested
    public class FlywayMigrateClosure {
        @Test
        void runsTheNestedClosure() {
            def plugin = new FlywayMigrationPlugin()
            def iWasCalled = false
            def nestedClosure = { -> iWasCalled = true }

            def flywayClosure = plugin.flywayMigrateClosure()
            flywayClosure.delegate = new MockWorkflowScript()
            flywayClosure(nestedClosure)

            assertThat(iWasCalled, equalTo(true))
        }

        @Test
        void setsTheListOfOptionalEnvironmentVariables() {
            def plugin = spy(new FlywayMigrationPlugin())
            def expectedList = ['KEY=value']
            doReturn(expectedList).when(plugin).buildEnvironmentVariableList(any(List.class))
            def flywayClosure = plugin.flywayMigrateClosure()
            def mockWorkflowScript = spy(new MockWorkflowScript())
            flywayClosure.delegate = mockWorkflowScript

            flywayClosure { -> }

            verify(mockWorkflowScript).withEnv(eq(expectedList), any(Closure.class))
        }
    }

    @Nested
    public class BuildEnvironmentVariableList {
        @Test
        void returnsEmptyListByDefault() {
            def plugin = new FlywayMigrationPlugin()

            def result = plugin.buildEnvironmentVariableList(null)

            assertThat(result, equalTo([]))
        }

        @Test
        void setsPasswordWhenVariableProvided() {
            def expectedVariable = 'MY_PASSWORD_VARIABLE'
            def expectedValue = 'somePass'
            FlywayMigrationPlugin.withPasswordFromEnvironmentVariable(expectedVariable)
            def plugin = new FlywayMigrationPlugin()
            def env = [(expectedVariable): expectedValue]

            def result = plugin.buildEnvironmentVariableList(env)

            assertThat(result, equalTo(["FLYWAY_PASSWORD=${expectedValue}"]))
        }

        @Test
        void setsUserWhenVariableProvided() {
            def expectedVariable = 'MY_USER_VARIABLE'
            def expectedValue = 'someUser'
            FlywayMigrationPlugin.withUserFromEnvironmentVariable(expectedVariable)
            def plugin = new FlywayMigrationPlugin()
            def env = [(expectedVariable): expectedValue]

            def result = plugin.buildEnvironmentVariableList(env)

            assertThat(result, equalTo(["FLYWAY_USER=${expectedValue}"]))
        }
    }

    @Nested
    public class BuildFlywayCommand {
        @Test
        void disablesEchoBeforeFlywayAndEnablesEchoAfterByDefault() {
            def flywayCommand = 'flyway foo'
            def command = mock(FlywayCommand.class)
            doReturn(flywayCommand).when(command).toString()
            def plugin = new FlywayMigrationPlugin()

            def result = plugin.buildFlywayCommand(command)

            assertThat(result, equalTo("set +x\n${flywayCommand}\nset -x".toString()))
        }

        @Test
        void returnsTheCommandIfEchoEnabled() {
            def flywayCommand = 'flyway foo'
            def command = mock(FlywayCommand.class)
            doReturn(flywayCommand).when(command).toString()
            def plugin = new FlywayMigrationPlugin()
            FlywayMigrationPlugin.withEchoEnabled()

            def result = plugin.buildFlywayCommand(command)

            assertThat(result, equalTo(flywayCommand))
        }
    }
}

