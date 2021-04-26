import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.any
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

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
    public class WithMappedEnvironmentVariable {
        @Test
        void isFluent() {
            def result = FlywayMigrationPlugin.withMappedEnvironmentVariable('TF_VAR_MY_USER', 'FLYWAY_USER')

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

        @Test
        void runsConfirmMigrationIfConfirmBeforeApplyAndHasPendingMigration() {
            def plugin = spy(new FlywayMigrationPlugin())
            doReturn(true).when(plugin).hasPendingMigration(any(Object.class))
            FlywayMigrationPlugin.confirmBeforeApplyingMigration()

            def flywayClosure = plugin.flywayMigrateClosure()
            flywayClosure.delegate = new MockWorkflowScript()
            flywayClosure { -> }

            verify(plugin).confirmMigration(any(Object.class))
        }

        @Test
        void doesNotRunConfirmMigrationIfNotConfirmBeforeApplyAndHasPendingMigration() {
            def plugin = spy(new FlywayMigrationPlugin())
            doReturn(true).when(plugin).hasPendingMigration(any(Object.class))

            def flywayClosure = plugin.flywayMigrateClosure()
            flywayClosure.delegate = new MockWorkflowScript()
            flywayClosure { -> }

            verify(plugin, times(0)).confirmMigration(any(Object.class))
        }

        @Test
        void doesNotRunConfirmMigrationIfConfirmBeforeApplyAndDoesNotHavePendingMigration() {
            def plugin = spy(new FlywayMigrationPlugin())
            doReturn(false).when(plugin).hasPendingMigration(any(Object.class))
            FlywayMigrationPlugin.confirmBeforeApplyingMigration()

            def flywayClosure = plugin.flywayMigrateClosure()
            flywayClosure.delegate = new MockWorkflowScript()
            flywayClosure { -> }

            verify(plugin, times(0)).confirmMigration(any(Object.class))
        }
    }

    @Nested
    public class HasPendingMigration {
        @Test
        void returnsTrueWhenShellReturnsTrueString() {
            def plugin = new FlywayMigrationPlugin()
            def workflowScript = spy(new MockWorkflowScript())
            doReturn('true').when(workflowScript).sh(any(Map.class))

            def result = plugin.hasPendingMigration(workflowScript)

            assertThat(result, equalTo(true))
        }

        @Test
        void returnsFalseWhenShellReturnsFalseString() {
            def plugin = new FlywayMigrationPlugin()
            def workflowScript = spy(new MockWorkflowScript())
            doReturn('false').when(workflowScript).sh(any(Map.class))

            def result = plugin.hasPendingMigration(workflowScript)

            assertThat(result, equalTo(false))
        }

        @Test
        void returnsFalseWhenShellReturnsAnyOtherString() {
            def plugin = new FlywayMigrationPlugin()
            def workflowScript = spy(new MockWorkflowScript())
            doReturn('blahblah').when(workflowScript).sh(any(Map.class))

            def result = plugin.hasPendingMigration(workflowScript)

            assertThat(result, equalTo(false))
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
        void mapsEnvironmentVariable() {
            def toVariable = 'FLYWAY_USER'
            def fromVariable = 'MY_USER_VARIABLE'
            def fromValue = 'someUser'
            FlywayMigrationPlugin.withMappedEnvironmentVariable(fromVariable, toVariable)
            def plugin = new FlywayMigrationPlugin()
            def env = [(fromVariable): fromValue]

            def result = plugin.buildEnvironmentVariableList(env)

            assertThat(result, equalTo(["${toVariable}=${fromValue}"]))
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

        @Nested
        public class WithConfirmBeforeApplyingMigration {
            @Test
            void prefixesFlywayCommandWithPipelineFail() {
                def plugin = spy(new FlywayMigrationPlugin())
                FlywayMigrationPlugin.confirmBeforeApplyingMigration()

                def result = plugin.buildFlywayCommand(mock(FlywayCommand.class))

                assertThat(result, containsString("set -o pipefail"))
            }

            @Test
            void pipesFlywayCommandWithToFileTee() {
                def plugin = spy(new FlywayMigrationPlugin())
                FlywayMigrationPlugin.confirmBeforeApplyingMigration()

                def result = plugin.buildFlywayCommand(mock(FlywayCommand.class))

                assertThat(result, containsString("| tee flyway_output.txt"))
            }
        }
    }

    @Nested
    public class ConfirmBeforeApplyingMigration {
        @Test
        void isFluent() {
            def result = FlywayMigrationPlugin.confirmBeforeApplyingMigration()

            assertThat(result, equalTo(FlywayMigrationPlugin.class))
        }
    }
}

