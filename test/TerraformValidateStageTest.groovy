import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.hamcrest.Matchers.isA
import static org.junit.Assert.assertThat

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformValidateStageTest {
    @After
    void resetPlugins() {
        TerraformValidateStage.resetPlugins()
    }

    public class PipelineConfiguration {
        @Test
        void returnsAJobDslClosure() {
            def validateStage = new TerraformValidateStage()

            def result = validateStage.pipelineConfiguration()

            assertThat(result, isA(Closure.class))
        }

        // This should be split into separate tests, and assert behavior
        @Test
        void justExerciseClosureNoAssertions() {
            Jenkinsfile.instance = spy(new Jenkinsfile())
            Jenkinsfile.original = new DummyJenkinsfile()
            def validateStage = new TerraformValidateStage()

            def closure = validateStage.pipelineConfiguration()
            closure.delegate = Jenkinsfile.original
            closure()
        }
    }

    public class Then {
        @Test
        void nextStageisCalled() {
            def stage  = new TerraformValidateStage()
            def stage2 = mock(Stage.class)

            def result = stage.then(stage2)

            assertThat(result, isA(BuildGraph.class))
        }
    }

    public class Build {
        @Test
        void justExerciseNoAssertions() {
            def stage  = new TerraformValidateStage()

            stage.build()
        }
    }

    public class Decorate {
        @Test
        void justExerciseNoAssertions() {
            def stage  = new TerraformValidateStage()

            stage.decorate { }
        }
    }
}
