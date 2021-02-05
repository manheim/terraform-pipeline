import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class WithAwsPluginTest {
    @AfterEach
    void reset() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    @Nested
    public class Init {
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

            assertThat(result, equalTo(WithAwsPlugin))
        }
    }

    @Nested
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

    @Nested
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

