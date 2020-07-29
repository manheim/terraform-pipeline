import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class DestroyPluginTest {

    @Before
    void resetJenkinsEnv() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    public class Init {

        @After
        void resetPlugins() {
            TerraformEnvironmentStage.resetPlugins()
        }

        @Before
        void resetParams() {
            TerraformEnvironmentStage.resetParams()
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            DestroyPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(DestroyPlugin.class)))

            Collection actualParms = TerraformEnvironmentStage.getParams()
            assertThat(actualParms, is(empty()));
        }

        @Test
        void modifiesConfirmApplyPlugin() {
            DestroyPlugin.init()

            String confirmMessage = ConfirmApplyPlugin.confirmMessage
            String okMessage = ConfirmApplyPlugin.okMessage

            assertEquals(confirmMessage, 'WARNING! Are you absolutely sure the plan above is correct? Your environment will be IMMEDIATELY DESTROYED via "terraform destroy"')
            assertEquals(okMessage, "Run terraform DESTROY now")
        }

    }

    public class Apply {

        @Test
        void setStrategyForTerraformEnvironmentStage()  {
            DestroyPlugin plugin = new DestroyPlugin()
            def environment = spy(new TerraformEnvironmentStage())

            plugin.apply(environment)

            verify(environment, times(1)).withStrategy(any(DestroyStrategy.class))
        }
    }
}
