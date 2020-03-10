import static org.mockito.Mockito.spy
import static org.mockito.Mockito.doReturn

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class BuildStageTest {

    public class Build {
        @After
        void reset() {
            Jenkinsfile.original = null
        }

        @Test
        void buildsWithoutError() {
            def expectedPipelineConfig = { -> }
            Jenkinsfile.original = new Expando()
            Jenkinsfile.original.ApplyJenkinsfileClosure = { closure -> }

            BuildStage stage = spy(new BuildStage())
            doReturn(expectedPipelineConfig).when(stage).pipelineConfiguration()

            stage.build()
        }
    }
}
