import static org.mockito.Mockito.spy
import static org.mockito.Mockito.doReturn

import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner


@RunWith(HierarchicalContextRunner.class)
class BuildStageTest {

    public class Build {
        @Test
        void buildsWithoutError() {
            BuildStage stage = spy(new BuildStage())

            doReturn { -> }.when(stage).pipelineConfiguration()

            stage.build()
        }
    }
}
