import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class WithAwsPluginTest {
    @After
    void reset() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
        WithAwsPlugin.reset()
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    public class Init {
        @After
        void resetPlugins() {
            TerraformEnvironmentStage.resetPlugins()
        }

        @Test
        void modifiesTerraformEnvironmentStage() {
            WithAwsPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(WithAwsPlugin.class)))
        }
    }

    public class WithRole {
        @Test
        void isFluentAndReturnsThePluginClass() {
            def result = WithAwsPlugin.withRole()

            assertTrue(result == WithAwsPlugin.class)
        }
    }

    public class WithImplicitRole {
        @Test
        void returnsGenericRoleIfPresent() {
            def expectedRole = "myRole"
            def plugin = new WithAwsPlugin()
            configureJenkins(env: [AWS_ROLE_ARN: expectedRole])

            plugin.withRole()

            def actualRole = plugin.getRole()
            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void returnsEnvironmentSpecificRoleIfPresent() {
            def expectedRole = "myRole"
            def plugin = new WithAwsPlugin()
            configureJenkins(env: [QA_AWS_ROLE_ARN: expectedRole])

            plugin.withRole()

            def actualRole = plugin.getRole('qa')
            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void returnsCaseInsensitiveEnvironmentSpecificRoleIfPresent() {
            def expectedRole = "myRole"
            def plugin = new WithAwsPlugin()
            configureJenkins(env: [qa_AWS_ROLE_ARN: expectedRole])

            plugin.withRole()

            def actualRole = plugin.getRole('qa')
            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void prefersGenericRoleOverEnvironmentRole() {
            def expectedRole = "correctRole"
            def plugin = new WithAwsPlugin()
            configureJenkins(env: [
                AWS_ROLE_ARN: expectedRole,
                QA_AWS_ROLE_ARN: 'incorrectRole'
            ])

            plugin.withRole()

            def actualRole = plugin.getRole('qa')
            assertThat(actualRole, is(expectedRole))
        }
    }

    public class WithExplicitRole {
        @Test
        void returnsProvidedRole() {
            def expectedRole = "myRole"
            def plugin = new WithAwsPlugin()

            plugin.withRole(expectedRole)

            def actualRole = plugin.getRole()

            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void prefersProvidedRoleOverGenericRole() {
            def expectedRole = "correctRole"
            def plugin = new WithAwsPlugin()
            configureJenkins(env: [AWS_ROLE_ARN: 'incorrectRole'])

            plugin.withRole(expectedRole)

            def actualRole = plugin.getRole()

            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void prefersProvidedRoleOverEnvironmntSpecificRole() {
            def expectedRole = "correctRole"
            def plugin = new WithAwsPlugin()
            configureJenkins(env: [QA_AWS_ROLE_ARN: 'incorrectRole'])

            plugin.withRole(expectedRole)

            def actualRole = plugin.getRole('qa')

            assertThat(actualRole, is(expectedRole))
        }
    }
}

