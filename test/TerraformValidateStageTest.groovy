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
}
