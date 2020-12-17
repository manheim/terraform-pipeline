import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
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
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class HookPointTest {

    public class Constructor {
        @Test
        void setsHookName() {
            def hp = new HookPoint("foo")
            assertEquals(hp.getName(), "foo")
        }
    }

    public class IsConfigured {
        @Test
        void falseIfAllNull() {
            def hp = new HookPoint("foo")
            assertFalse(hp.isConfigured())
        }

        @Test
        void trueIfRunBefore() {
            def hp = new HookPoint("foo")
            hp.runBefore = 'bar'
            assertTrue(hp.isConfigured())
        }

        @Test
        void trueIfRunAfterOnSuccess() {
            def hp = new HookPoint("foo")
            hp.runAfterOnSuccess = 'bar'
            assertTrue(hp.isConfigured())
        }

        @Test
        void trueIfRunAfterOnFailure() {
            def hp = new HookPoint("foo")
            hp.runAfterOnFailure = 'bar'
            assertTrue(hp.isConfigured())
        }

        @Test
        void trueIfRunAfterAlways() {
            def hp = new HookPoint("foo")
            hp.runAfterAlways = 'bar'
            assertTrue(hp.isConfigured())
        }
    }

    public class GetClosure {
        @After
        public void reset() {
            Jenkinsfile.instance = null
            TerraformEnvironmentStage.reset()
            TerraformEnvironmentStageShellHookPlugin.reset()
        }

        @Test
        void runsAllConfiguredHooks() {
            def hp = new HookPoint("foo")
            hp.runBefore = 'run-before'
            hp.runAfterOnSuccess = 'after-success'
            hp.runAfterOnFailure = 'after-failure'
            hp.runAfterAlways = 'after-always'

            def dummyJenkinsfile = mock(DummyJenkinsfile.class)

            def passedClosure = { -> sh 'passedClosure' }
            passedClosure.delegate = dummyJenkinsfile

            def result = hp.getClosure()
            result.delegate = dummyJenkinsfile
            result.call(passedClosure)

            InOrder inOrder = inOrder(dummyJenkinsfile);
            inOrder.verify(dummyJenkinsfile, times(1)).sh('run-before')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('passedClosure')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-success')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-always')
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(dummyJenkinsfile)
        }

        @Test
        @SuppressWarnings('EmptyCatchBlock')
        void runsHooksOnFailure() {
            def hp = new HookPoint("foo")
            hp.runBefore = 'run-before'
            hp.runAfterOnSuccess = 'after-success'
            hp.runAfterOnFailure = 'after-failure'
            hp.runAfterAlways = 'after-always'

            def dummyJenkinsfile = mock(DummyJenkinsfile.class)

            def passedClosure = { ->
                sh 'passedClosure'
                throw new Exception()
            }
            passedClosure.delegate = dummyJenkinsfile

            def result = hp.getClosure()
            result.delegate = dummyJenkinsfile

            try {
                result.call(passedClosure)
            } catch (Exception e) { }

            InOrder inOrder = inOrder(dummyJenkinsfile);
            inOrder.verify(dummyJenkinsfile, times(1)).sh('run-before')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('passedClosure')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-failure')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-always')
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(dummyJenkinsfile)
        }

        @Test
        void runsOnlyBeforeHook() {
            def hp = new HookPoint("foo")
            hp.runBefore = 'run-before'

            def dummyJenkinsfile = mock(DummyJenkinsfile.class)

            def passedClosure = { -> sh 'passedClosure' }
            passedClosure.delegate = dummyJenkinsfile

            def result = hp.getClosure()
            result.delegate = dummyJenkinsfile
            result.call(passedClosure)

            InOrder inOrder = inOrder(dummyJenkinsfile);
            inOrder.verify(dummyJenkinsfile, times(1)).sh('run-before')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('passedClosure')
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(dummyJenkinsfile)
        }

        @Test
        void runsOnlyAfterSuccessHook() {
            def hp = new HookPoint("foo")
            hp.runAfterOnSuccess = 'after-success'

            def dummyJenkinsfile = mock(DummyJenkinsfile.class)

            def passedClosure = { -> sh 'passedClosure' }
            passedClosure.delegate = dummyJenkinsfile

            def result = hp.getClosure()
            result.delegate = dummyJenkinsfile
            result.call(passedClosure)

            InOrder inOrder = inOrder(dummyJenkinsfile);
            inOrder.verify(dummyJenkinsfile, times(1)).sh('passedClosure')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-success')
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(dummyJenkinsfile)
        }

        @Test
        void runsOnlyAfterAlwaysHook() {
            def hp = new HookPoint("foo")
            hp.runAfterAlways = 'after-always'

            def dummyJenkinsfile = mock(DummyJenkinsfile.class)

            def passedClosure = { -> sh 'passedClosure' }
            passedClosure.delegate = dummyJenkinsfile

            def result = hp.getClosure()
            result.delegate = dummyJenkinsfile
            result.call(passedClosure)

            InOrder inOrder = inOrder(dummyJenkinsfile);
            inOrder.verify(dummyJenkinsfile, times(1)).sh('passedClosure')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-always')
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(dummyJenkinsfile)
        }

        @Test
        @SuppressWarnings('EmptyCatchBlock')
        void runsOnlyAfterAlwaysHookOnFailure() {
            def hp = new HookPoint("foo")
            hp.runAfterAlways = 'after-always'

            def dummyJenkinsfile = mock(DummyJenkinsfile.class)

            def passedClosure = { ->
                sh 'passedClosure'
                throw new Exception()
            }
            passedClosure.delegate = dummyJenkinsfile

            def result = hp.getClosure()
            result.delegate = dummyJenkinsfile
            try {
                result.call(passedClosure)
            } catch (Exception e) { }

            InOrder inOrder = inOrder(dummyJenkinsfile);
            inOrder.verify(dummyJenkinsfile, times(1)).sh('passedClosure')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-always')
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(dummyJenkinsfile)
        }

        @Test
        @SuppressWarnings('EmptyCatchBlock')
        void runsOnlyAfterFailureHook() {
            def hp = new HookPoint("foo")
            hp.runAfterOnFailure = 'after-failure'

            def dummyJenkinsfile = mock(DummyJenkinsfile.class)

            def passedClosure = { ->
                sh 'passedClosure'
                throw new Exception()
            }
            passedClosure.delegate = dummyJenkinsfile

            def result = hp.getClosure()
            result.delegate = dummyJenkinsfile
            try {
                result.call(passedClosure)
            } catch (Exception e) { }

            InOrder inOrder = inOrder(dummyJenkinsfile);
            inOrder.verify(dummyJenkinsfile, times(1)).sh('passedClosure')
            inOrder.verify(dummyJenkinsfile, times(1)).sh('after-failure')
            inOrder.verifyNoMoreInteractions()
            verifyNoMoreInteractions(dummyJenkinsfile)
        }
    }
}

@RunWith(HierarchicalContextRunner.class)
class TerraformEnvironmentStageShellHookPluginTest {
    def hookKeys = [ALL, INIT_COMMAND, PLAN, PLAN_COMMAND, APPLY, APPLY_COMMAND]

    @After
    public void reset() {
        Jenkinsfile.instance = null
        TerraformEnvironmentStage.reset()
        TerraformEnvironmentStageShellHookPlugin.reset()
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getOrganization()).thenReturn(config.organization)
        when(Jenkinsfile.instance.getRepoName()).thenReturn(config.repoName)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    public class Hooks {
        @Test
        void hasAllHooksUnconfigured() {
            TerraformEnvironmentStageShellHookPlugin.reset()
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            assertEquals(hooks.size(), 6)
            hookKeys.each {
                assertThat(hooks[it], instanceOf(HookPoint))
                assertFalse(hooks[it].isConfigured())
                assertEquals(hooks[it].getName(), it)
            }
        }
    }

    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            TerraformEnvironmentStageShellHookPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformEnvironmentStageShellHookPlugin.class)))
        }
    }

    public class WithHook {
        @Test
        void setDefaultOnSuccess() {
            TerraformEnvironmentStageShellHookPlugin.withHook(APPLY, 'foo bar')
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == APPLY) {
                    assertTrue(hooks[it].isConfigured())
                    assertEquals(hooks[it].runAfterOnSuccess, 'foo bar')
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                } else {
                    assertFalse(hooks[it].isConfigured())
                }
            }
        }

        @Test
        void setRunBefore() {
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN_COMMAND, 'foo bar', WhenToRun.BEFORE)
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == PLAN_COMMAND) {
                    assertTrue(hooks[it].isConfigured())
                    assertEquals(hooks[it].runBefore, 'foo bar')
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertFalse(hooks[it].isConfigured())
                }
            }
        }

        @Test
        void setRunAfterOnFailure() {
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN, 'foo bar', WhenToRun.ON_FAILURE)
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == PLAN) {
                    assertTrue(hooks[it].isConfigured())
                    assertEquals(hooks[it].runAfterOnFailure, 'foo bar')
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertFalse(hooks[it].isConfigured())
                }
            }
        }

        @Test
        void setRunAfterAlways() {
            TerraformEnvironmentStageShellHookPlugin.withHook(PLAN, 'foo bar', WhenToRun.AFTER)
            def hooks = TerraformEnvironmentStageShellHookPlugin.hooks
            hookKeys.each {
                if (it == PLAN) {
                    assertTrue(hooks[it].isConfigured())
                    assertEquals(hooks[it].runAfterAlways, 'foo bar')
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertFalse(hooks[it].isConfigured())
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
                    assertTrue(hooks[it].isConfigured())
                    assertEquals(hooks[it].runAfterAlways, 'baz')
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else {
                    assertFalse(hooks[it].isConfigured())
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
                    assertTrue(hooks[it].isConfigured())
                    assertEquals(hooks[it].runBefore, 'all-before')
                    assertEquals(hooks[it].runAfterAlways, 'all-after-always')
                    assertNull(hooks[it].runAfterOnFailure)
                    assertNull(hooks[it].runAfterOnSuccess)
                } else if (it == INIT_COMMAND) {
                    assertTrue(hooks[it].isConfigured())
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertEquals(hooks[it].runAfterOnSuccess, 'init-command-after-success')
                } else if (it == PLAN) {
                    assertTrue(hooks[it].isConfigured())
                    assertNull(hooks[it].runBefore)
                    assertNull(hooks[it].runAfterAlways)
                    assertNull(hooks[it].runAfterOnFailure)
                    assertEquals(hooks[it].runAfterOnSuccess, 'plan-after-success')
                } else {
                    assertFalse(hooks[it].isConfigured())
                }
            }
        }
    }

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
