import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*

@RunWith(HierarchicalContextRunner.class)
class ConditionalApplyPluginTest {
    @After
    public void reset() {
        Jenkinsfile.instance = null
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    @Test
    void modifiesTerraformEnvironmentStageByDefault() {
        Collection actualPlugins = TerraformEnvironmentStage.getPlugins()

        assertThat(actualPlugins, hasItem(instanceOf(ConditionalApplyPlugin.class)))
    }

    class ShouldApply {
        @Test
        void returnsTrueForMaster() {
            configureJenkins(env: [ BRANCH_NAME: 'master' ])
            def plugin = new ConditionalApplyPlugin()

            assertTrue(plugin.shouldApply())
        }

        @Test
        void returnsFalseForNonMaster() {
            configureJenkins(env: [ BRANCH_NAME: 'notMaster' ])
            def plugin = new ConditionalApplyPlugin()

            assertFalse(plugin.shouldApply())
        }

        @Test
        void returnsTrueWhenBranchIsUnknown() {
            configureJenkins(env: [ : ])
            def plugin = new ConditionalApplyPlugin()

            assertTrue(plugin.shouldApply())
        }
    }
}

