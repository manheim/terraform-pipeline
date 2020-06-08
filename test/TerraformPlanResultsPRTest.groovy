import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static TerraformEnvironmentStage.PLAN;

import org.junit.Test
import org.junit.After
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformPlanResultsPRTest {

    public class Init {
        @After
        void resetPlugins() {
            TerraformPlanCommand.resetPlugins()
            TerraformEnvironmentStage.resetPlugins()
        }

        @Test
        void modifiesTerraformPlanCommand() {
            TerraformPlanResultsPR.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformPlanResultsPR.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            TerraformPlanResultsPR.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformPlanResultsPR.class)))
        }
    }

    public class Apply {

        @Test
        void addsTeeArgumentToTerraformPlan() {
            TerraformPlanResultsPR plugin = new TerraformPlanResultsPR()
            plugin.withLandscape(false)
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(" -out=tfplan 2>plan.err | tee plan.out"))
        }

        @Test
        void addsTeeAndLandscapeArgumentToTerraformPlan() {
            TerraformPlanResultsPR plugin = new TerraformPlanResultsPR()
            plugin.withLandscape(true)
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(" -out=tfplan 2>plan.err | landscape | tee plan.out"))
        }

        @Test
        void decoratesTheTerraformEnvironmentStage()  {
            TerraformPlanResultsPR plugin = new TerraformPlanResultsPR()
            def environment = spy(new TerraformEnvironmentStage())

            plugin.apply(environment)

            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.PLAN), any(Closure.class))
        }

    }
}
