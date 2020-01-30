import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(HierarchicalContextRunner.class)
class RegressionStageTest {

    public class AutomationRepo {
        @After
        void reset() {
            TerraformEnvironmentStage.resetPlugins()
            Jenkinsfile.instance = mock(Jenkinsfile.class)
        }

        private configureJenkins(Map config = [:]) {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
            when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
        }

        @Test
        void automationRepoSpecifiedSuccessfullyCallApply(){
            RegressionStagePlugin fakePlugin = mock(RegressionStagePlugin.class)
            RegressionStage.addPlugin(fakePlugin)

            configureJenkins()
            RegressionStage stage = new RegressionStage().withScm("git:someHost:someUser/someRepo.git")
            stage.build()

            verify(fakePlugin).apply(stage)
        }

        @Test
        void automationRepoAndAppRepoSpecifiedSuccessfullyCallApply(){
            RegressionStagePlugin fakePlugin = mock(RegressionStagePlugin.class)
            RegressionStage.addPlugin(fakePlugin)

            configureJenkins()
            RegressionStage stage = new RegressionStage().withScm("git:someHost:someUser/someRepo.git")
                                                         .withScm("git:someHost:someUser/someOtherRepo.git")
            stage.build()

            verify(fakePlugin).apply(stage)
        }

        @Test
        void automationRepoAndAppRepoWithChangeDirectorySpecifiedSuccessfullyCallApply(){
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
        void noAutomationRepoSpecifiedSuccessfullyCallApply(){
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
            TerraformEnvironmentStage.resetPlugins()
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
