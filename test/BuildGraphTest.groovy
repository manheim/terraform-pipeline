import static org.mockito.Mockito.inOrder
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import org.mockito.InOrder

@RunWith(HierarchicalContextRunner.class)
class BuildGraphTest {
    public class WithASingleStage {
        @Test
        void buildsTheStageThatItWasCreatedWith() {
            Stage startStage = mock(Stage.class)
            BuildGraph graph = new BuildGraph(startStage)

            graph.build()

            verify(startStage, times(1)).build()
        }
    }

    public class WithMultipleStages {
        @Test
        void buildsTheStagesInOrder() {
            Stage stage1 = mock(Stage.class)
            Stage stage2 = mock(Stage.class)
            Stage stage3 = mock(Stage.class)
            Stage stage4 = mock(Stage.class)

            BuildGraph graph = new BuildGraph(stage1).then(stage2)
                                                     .then(stage3)
                                                     .then(stage4)
                                                     .build()

            InOrder inOrder = inOrder(stage1, stage2, stage3, stage4);
            inOrder.verify(stage1, times(1)).build()
            inOrder.verify(stage2, times(1)).build()
            inOrder.verify(stage3, times(1)).build()
            inOrder.verify(stage4, times(1)).build()
        }
    }
}
