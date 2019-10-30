import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.doReturn

@RunWith(HierarchicalContextRunner.class)
class BuildStageTest {

    public class Build {
        @Test
        void buildsWithoutError() {
            BuildStage stage = spy(new BuildStage())

            doReturn({ -> }).when(stage).pipelineConfiguration()

            stage.build()
        }
    }
}
