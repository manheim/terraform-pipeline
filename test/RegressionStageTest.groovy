import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class RegressionStageTest {

    public class AutomationRepo {
        @After
        void reset() {
            TerraformEnvironmentStage.reset()
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            Jenkinsfile.original = null
        }

        private configureJenkins(Map config = [:]) {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            Jenkinsfile.original = new Expando()
            Jenkinsfile.original.ApplyJenkinsfileClosure = { closure -> }
            when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
            when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
        }

        @Test
        void automationRepoSpecifiedSuccessfullyCallApply() {
            RegressionStagePlugin fakePlugin = mock(RegressionStagePlugin.class)
            RegressionStage.addPlugin(fakePlugin)

            configureJenkins()
            RegressionStage stage = new RegressionStage().withScm("git:someHost:someUser/someRepo.git")
            stage.build()

            verify(fakePlugin).apply(stage)
        }

        @Test
        void automationRepoAndAppRepoSpecifiedSuccessfullyCallApply() {
            RegressionStagePlugin fakePlugin = mock(RegressionStagePlugin.class)
            RegressionStage.addPlugin(fakePlugin)

            configureJenkins()
            RegressionStage stage = new RegressionStage().withScm("git:someHost:someUser/someRepo.git")
                                                         .withScm("git:someHost:someUser/someOtherRepo.git")
            stage.build()

            verify(fakePlugin).apply(stage)
        }

        @Test
        void automationRepoAndAppRepoWithChangeDirectorySpecifiedSuccessfullyCallApply() {
            RegressionStagePlugin fakePlugin = mock(RegressionStagePlugin.class)
            RegressionStage.addPlugin(fakePlugin)

            configureJenkins()
            RegressionStage stage = new RegressionStage().withScm("git:someHost:someUser/someRepo.git")
                    .withScm("git:someHost:someUser/someOtherRepo.git")
                    .changeDirectory("someDir")
            stage.build()

            verify(fakePlugin).apply(stage)
        }

        @Test
        void noAutomationRepoSpecifiedSuccessfullyCallApply() {
            RegressionStagePlugin fakePlugin = mock(RegressionStagePlugin.class)
            RegressionStage.addPlugin(fakePlugin)

            configureJenkins()
            RegressionStage stage = new RegressionStage()
            stage.build()

            verify(fakePlugin).apply(stage)
        }
    }

    public class AddedPlugins {
        @After
        void resetPlugins() {
            TerraformEnvironmentStage.reset()
        }

        @Test
        void willHaveApplyCalled() {
            RegressionStagePlugin fakePlugin = mock(RegressionStagePlugin.class)
            RegressionStage.addPlugin(fakePlugin)

            RegressionStage stage = new RegressionStage()
            stage.applyPlugins()

            verify(fakePlugin).apply(stage)
        }
    }
}
