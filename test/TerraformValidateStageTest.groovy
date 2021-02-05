import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.instanceOf

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformValidateStageTest {
    @Nested
    public class PipelineConfiguration {
        @Test
        void returnsAJobDslClosure() {
            def validateStage = new TerraformValidateStage()

            def result = validateStage.pipelineConfiguration()

            assertThat(result, is(instanceOf(Closure.class)))
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

    @Nested
    public class Then {
        @Test
        void nextStageisCalled() {
            def stage  = new TerraformValidateStage()
            def stage2 = mock(Stage.class)

            def result = stage.then(stage2)

            assertThat(result, is(instanceOf(BuildGraph.class)))
        }
    }

    @Nested
    public class Build {
        @Test
        void justExerciseNoAssertions() {
            def stage  = new TerraformValidateStage()
            MockJenkinsfile.withMockedOriginal()

            stage.build()
        }
    }

    @Nested
    public class Decorate {
        @Test
        void justExerciseNoAssertions() {
            def stage  = new TerraformValidateStage()

            stage.decorate { }
        }
    }
}
