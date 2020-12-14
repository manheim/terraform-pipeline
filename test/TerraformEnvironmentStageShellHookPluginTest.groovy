import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformEnvironmentStageShellHookPluginTest {
    @After
    public void reset() {
        Jenkinsfile.instance = null
        TerraformEnvironmentStage.reset()
        TerraformEnvironmentStageShellHookPlugin.reset()
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getOrganization()).thenReturn(config.organization)
        when(Jenkinsfile.instance.getRepoName()).thenReturn(config.repoName)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            TerraformEnvironmentStageShellHookPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformEnvironmentStageShellHookPlugin.class)))
        }
    }

}
