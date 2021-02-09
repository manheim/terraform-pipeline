import static org.mockito.Mockito.spy
import static org.mockito.Mockito.doReturn

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuildStageTest {

    @Nested
    public class Build {
        @Test
        void buildsWithoutError() {
            def expectedPipelineConfig = { -> }
            MockJenkinsfile.withMockedOriginal()

            BuildStage stage = spy(new BuildStage())
            doReturn(expectedPipelineConfig).when(stage).pipelineConfiguration()

            stage.build()
        }
    }
}
