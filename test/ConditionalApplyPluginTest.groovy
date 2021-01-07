import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class ConditionalApplyPluginTest {
    @After
    public void reset() {
        Jenkinsfile.instance = null
        ConditionalApplyPlugin.reset()
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
        void returnsTrueForMasterByDefault() {
            configureJenkins(env: [ BRANCH_NAME: 'master' ])
            def plugin = new ConditionalApplyPlugin()

            assertTrue(plugin.shouldApply())
        }

        @Test
        void returnsFalseForNonMasterByDefault() {
            configureJenkins(env: [ BRANCH_NAME: 'notMaster' ])
            def plugin = new ConditionalApplyPlugin()

            assertFalse(plugin.shouldApply())
        }

        @Test
        void returnsTrueForFirstConfiguredBranch() {
            configureJenkins(env: [ BRANCH_NAME: 'qa' ])
            ConditionalApplyPlugin.withBranchApplyEnabledFor(['qa', 'someOtherBranch'])
            def plugin = new ConditionalApplyPlugin()

            assertTrue(plugin.shouldApply())
        }

        @Test
        void returnsTrueForOtherConfiguredBranches() {
            configureJenkins(env: [ BRANCH_NAME: 'someOtherBranch' ])
            ConditionalApplyPlugin.withBranchApplyEnabledFor(['qa', 'someOtherBranch'])
            def plugin = new ConditionalApplyPlugin()

            assertTrue(plugin.shouldApply())
        }

        @Test
        void returnsFalseForNonMatchingBranch() {
            configureJenkins(env: [ BRANCH_NAME: 'notQa' ])
            ConditionalApplyPlugin.withBranchApplyEnabledFor(['qa', 'someOtherBranch'])
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

