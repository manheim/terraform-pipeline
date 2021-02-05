import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.assertNull
import static org.mockito.Mockito.inOrder
import static org.mockito.Mockito.when
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.verifyNoMoreInteractions

import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.INIT_COMMAND
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.PLAN_COMMAND
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.APPLY_COMMAND

import org.mockito.InOrder

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformEnvironmentStageShellHookPluginTest {
    def hookKeys = [ALL, INIT_COMMAND, PLAN, PLAN_COMMAND, APPLY, APPLY_COMMAND]

    @AfterEach
    public void reset() {
        Jenkinsfile.instance = null
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getOrganization()).thenReturn(config.organization)
        when(Jenkinsfile.instance.getRepoName()).thenReturn(config.repoName)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    @Nested
    public class Hooks {
        @Test
        void hasAllHooksUnconfigured() {
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            assertThat(hooks.size(), equalTo(6))
            hookKeys.each {
                assertThat(hooks[it], instanceOf(HookPoint))
                assertThat(hooks[it].isConfigured(), equalTo(false))
                assertThat(it, equalTo(hooks[it].getName()))
            }
        }
    }

    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            TerraformEnvironmentStageShellHookPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformEnvironmentStageShellHookPlugin.class)))
        }
    }

    @Nested
    public class WithHook {
        @Test
        void setDefaultOnSuccess() {
            TerraformEnvironmentStageShellHookPlugin.withHook(APPLY, 'foo bar')
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == APPLY) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertThat(hooks[it].runAfterOnSuccess, equalTo('foo bar'))
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                } else {
                    assertThat(hooks[it].isConfigured(), equalTo(false))
                }
            }
        }

        @Test
        void setRunBefore() {
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN_COMMAND, 'foo bar', WhenToRun.BEFORE)
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == PLAN_COMMAND) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertThat(hooks[it].runBefore, equalTo('foo bar'))
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertThat(hooks[it].isConfigured(), equalTo(false))
                }
            }
        }

        @Test
        void setRunAfterOnFailure() {
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN, 'foo bar', WhenToRun.ON_FAILURE)
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == PLAN) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertThat(hooks[it].runAfterOnFailure, equalTo('foo bar'))
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertThat(hooks[it].isConfigured(), equalTo(false))
                }
            }
        }

        @Test
        void setRunAfterAlways() {
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN, 'foo bar', WhenToRun.AFTER)
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == PLAN) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertThat(hooks[it].runAfterAlways, equalTo('foo bar'))
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertThat(hooks[it].isConfigured(), equalTo(false))
                }
            }
        }

        @Test
        void onlyLastCommandIsSet() {
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN, 'foo bar', WhenToRun.AFTER)
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN, 'baz', WhenToRun.AFTER)
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == PLAN) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertThat(hooks[it].runAfterAlways, equalTo('baz'))
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertThat(hooks[it].isConfigured(), equalTo(false))
                }
            }
        }

        @Test
        void setMultipleHooks() {
            TerraformEnvironmentStageShellHookPlugin.withHook(ALL, 'all-before', WhenToRun.BEFORE)
                                                    .withHook(ALL, 'all-after-always', WhenToRun.AFTER)
                                                    .withHook(INIT_COMMAND, 'init-command-after-success')
                                                    .withHook(PLAN, 'plan-after-success')
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == ALL) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertThat(hooks[it].runBefore, equalTo('all-before'))
                    assertThat(hooks[it].runAfterAlways, equalTo('all-after-always'))
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else if (it == INIT_COMMAND) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertThat(hooks[it].runAfterOnSuccess, equalTo('init-command-after-success'))
                } else if (it == PLAN) {
                    assertThat(hooks[it].isConfigured(), equalTo(true))
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertThat(hooks[it].runAfterOnSuccess, equalTo('plan-after-success'))
                } else {
                    assertThat(hooks[it].isConfigured(), equalTo(false))
                }
            }
        }
    }

    @Nested
    public class Apply {
        @Test
        void testApply() {
            TerraformEnvironmentStageShellHookPlugin.hooks = [
                (ALL): spy(new HookPoint()),
                (PLAN): spy(new HookPoint()),
                (APPLY): spy(new HookPoint())
            ]
            def cAll = { 1 }
            doReturn(cAll).when(TerraformEnvironmentStageShellHookPlugin.hooks[ALL]).getClosure()
            doReturn(true).when(TerraformEnvironmentStageShellHookPlugin.hooks[ALL]).isConfigured()
            def cPlan = { 2 }
            doReturn(cPlan).when(TerraformEnvironmentStageShellHookPlugin.hooks[PLAN]).getClosure()
            doReturn(false).when(TerraformEnvironmentStageShellHookPlugin.hooks[PLAN]).isConfigured()
            def cApply = { 3 }
            doReturn(cApply).when(TerraformEnvironmentStageShellHookPlugin.hooks[APPLY]).getClosure()
            doReturn(true).when(TerraformEnvironmentStageShellHookPlugin.hooks[APPLY]).isConfigured()

            TerraformEnvironmentStage mockStage = mock(TerraformEnvironmentStage.class)

            def plugin = new TerraformEnvironmentStageShellHookPlugin()
            plugin.apply(mockStage)
            InOrder inOrder = inOrder(mockStage);
            inOrder.verify(mockStage, times(1)).decorate(ALL, cAll)
            inOrder.verify(mockStage, times(1)).decorate(APPLY, cApply)
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(mockStage)
        }
    }

}
