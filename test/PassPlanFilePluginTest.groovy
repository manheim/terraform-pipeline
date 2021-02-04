import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PassPlanFilePluginTest {
    @BeforeEach
    void resetJenkinsEnv() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    @Nested
    public class Init {
        @AfterEach
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

        @Test
        void modifiesTerraformPlanCommand() {
            PassPlanFilePlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PassPlanFilePlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            PassPlanFilePlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PassPlanFilePlugin.class)))
        }

    }

    @Nested
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
            TerraformPlanCommand command = new TerraformPlanCommand("dev")
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-out=tfplan-dev"))
        }

        @Test
        void addsArgumentToTerraformApply() {
            PassPlanFilePlugin plugin = new PassPlanFilePlugin()
            TerraformApplyCommand command = new TerraformApplyCommand("dev")
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("tfplan-dev"))
        }

    }

    @Nested
    public class StashPlan {

        @Test
        void runsStashPlan() {
            def wasCalled = false
            def passedClosure = { -> wasCalled = true }
            def plugin = new PassPlanFilePlugin()

            def stashClosure = plugin.stashPlan('dev')
            stashClosure.delegate = new DummyJenkinsfile()
            stashClosure.call(passedClosure)

            assertThat(wasCalled, equalTo(true))
        }

    }

    @Nested
    public class UnstashPlan {

        @Test
        void runsUnstashPlan() {
            def wasCalled = false
            def passedClosure = { -> wasCalled = true }
            def plugin = new PassPlanFilePlugin()

            def unstashClosure = plugin.unstashPlan('dev')
            unstashClosure.delegate = new DummyJenkinsfile()
            unstashClosure.call(passedClosure)

            assertThat(wasCalled, equalTo(true))
        }

    }

}
