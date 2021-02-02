import static org.mockito.Mockito.spy
import static org.mockito.Mockito.doReturn

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuildStageTest {

    @Nested
    public class Build {
        @AfterEach
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
