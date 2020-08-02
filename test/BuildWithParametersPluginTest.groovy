import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.spy;

import org.junit.Test
import org.junit.After
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class BuildWithParametersPluginTest {
    private createJenkinsfileSpy() {
        def dummyJenkinsfile = spy(new DummyJenkinsfile())
        dummyJenkinsfile.docker = dummyJenkinsfile

        return dummyJenkinsfile
    }

    public class Init {
        @After
        void resetPlugins() {
            BuildStage.resetPlugins()
            TerraformValidateStage.resetPlugins()
            TerraformEnvironmentStage.reset()
            RegressionStage.resetPlugins()
        }

        @Test
        void modifiesBuildStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = BuildStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }

        @Test
        void modifiesTerraformValidateStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }

        @Test
        void modifiesRegressionStage() {
            BuildWithParametersPlugin.init()

            Collection actualPlugins = RegressionStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(BuildWithParametersPlugin.class)))
        }
    }
}

