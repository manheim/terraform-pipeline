import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class PassPlanFilePluginTest {
    @Before
    void resetJenkinsEnv() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    public class Init {
        @After
        void resetPlugins() {
            TerraformPlanCommand.resetPlugins()
            TerraformApplyCommand.resetPlugins()
            TerraformEnvironmentStage.reset()
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            PassPlanFilePlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PassPlanFilePlugin.class)))
        }

    }

    public class Apply {

        @Test
        void decoratesTheTerraformEnvironmentStage()  {
            PassPlanFilePlugin plugin = new PassPlanFilePlugin()
            def environment = spy(new TerraformEnvironmentStage())
            plugin.apply(environment)

            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.PLAN), any(Closure.class))
            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.APPLY), any(Closure.class))
        }

        @Test
        void addsArgumentToTerraformPlan() {
            PassPlanFilePlugin plugin = new PassPlanFilePlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()
            configureJenkins(env: [
                'FAIL_PLAN_ON_CHANGES': 'true'
            ])

            plugin.apply(command)

            String result = command.toString()
            String environment = command.getEnvironment()
            assertThat(result, containsString("-out=tfplan-" + environment))
        }

        @Test
        void addsArgumentToTerraformApply() {
            PassPlanFilePlugin plugin = new PassPlanFilePlugin()
            TerraformApplyCommand command = new TerraformApplyCommand()
            configureJenkins(env: [
                'FAIL_PLAN_ON_CHANGES': 'true'
            ])

            plugin.apply(command)

            String result = command.toString()
            String environment = command.getEnvironment()
            assertThat(result, containsString("tfplan-" + environment))
        }

    }

}
