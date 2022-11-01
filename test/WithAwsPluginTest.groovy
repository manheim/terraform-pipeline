import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class WithAwsPluginTest {
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
            MockJenkinsfile.withEnv(AWS_ROLE_ARN: expectedRole)

            plugin.withRole()

            def actualRole = plugin.getRole()
            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void returnsEnvironmentSpecificRoleIfPresent() {
            def expectedRole = "myRole"
            def plugin = new WithAwsPlugin()
            MockJenkinsfile.withEnv(QA_AWS_ROLE_ARN: expectedRole)

            plugin.withRole()

            def actualRole = plugin.getRole('qa')
            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void returnsCaseInsensitiveEnvironmentSpecificRoleIfPresent() {
            def expectedRole = "myRole"
            def plugin = new WithAwsPlugin()
            MockJenkinsfile.withEnv(qa_AWS_ROLE_ARN: expectedRole)

            plugin.withRole()

            def actualRole = plugin.getRole('qa')
            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void prefersGenericRoleOverEnvironmentRole() {
            def expectedRole = "correctRole"
            def plugin = new WithAwsPlugin()
            MockJenkinsfile.withEnv([
                AWS_ROLE_ARN: expectedRole,
                QA_AWS_ROLE_ARN: 'incorrectRole'
            ])

            plugin.withRole()

            def actualRole = plugin.getRole('qa')
            assertThat(actualRole, is(expectedRole))
        }
    }

    @Nested
    public class WithDefaultDuration {
        @Test
        void returnsDefaultDuration() {
            def expectedDuration = 3600
            def plugin = new WithAwsPlugin()
            MockJenkinsfile.withEnv(AWS_ROLE_ARN: 'foo')

            plugin.withRole()

            def actualDuration = plugin.getDuration()
            assertThat(actualDuration, is(expectedDuration))
        }
    }

    @Nested
    public class WithExplicitDuration {
        @Test
        void returnsExplicitDuration() {
            def expectedDuration = 43200
            def plugin = new WithAwsPlugin()

            plugin.withRole(duration: expectedDuration)

            def actualDuration = plugin.getDuration()

            assertThat(actualDuration, is(expectedDuration))
        }
    }
}
