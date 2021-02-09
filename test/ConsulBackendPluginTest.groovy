import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class ConsulBackendPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformInitCommand() {
            ConsulBackendPlugin.init()

            Collection actualPlugins = TerraformInitCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ConsulBackendPlugin.class)))
        }
    }

    @Nested
    public class Apply {
        @Nested
        public class PathBackendParameter {
            @Test
            void isAddedAndIsEnvironmentSpecific() {
                String repoSlug = 'myOrg/myRepo'
                MockJenkinsfile.withStandardizedRepoSlug(repoSlug).withEnv()

                String environment = "myEnv"
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=path=terraform/${repoSlug}_${environment}"))
            }

            @Test
            void isAddedAndUsesCustomizablePattern() {
                MockJenkinsfile.withStandardizedRepoSlug('someOrg/someRepo').withEnv()

                ConsulBackendPlugin.pathPattern = { String env -> "customPatternFor_${env}" }
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=path=customPatternFor_myEnv"))
            }
        }

        @Nested
        public class AddressBackendParameter {
            @Test
            void isNotAddedByDefault() {
                MockJenkinsfile.withEnv()
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand('someEnvironment')

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, not(containsString("-backend-config=address")))
            }

            @Test
            void isAddedIfEnvironmentVariablePresent() {
                String expectedConsulAddress = 'someAddress'
                MockJenkinsfile.withEnv(DEFAULT_CONSUL_ADDRESS: expectedConsulAddress)
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand('someEnvironment')

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=address=${expectedConsulAddress}"))
            }

            @Test
            void isAddedIfExplicitlySet() {
                String expectedConsulAddress = 'someAddress'
                ConsulBackendPlugin.defaultAddress = expectedConsulAddress
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand('someEnvironment')

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=address=${expectedConsulAddress}"))
            }

            @Test
            void isAddedAndPrefersTheExplicitValueOverTheDefaultEnvironmentValue() {
                String expectedConsulAddress = 'theRightValue'
                ConsulBackendPlugin.defaultAddress = expectedConsulAddress
                MockJenkinsfile.withEnv(DEFAULT_CONSUL_ADDRESS: 'theWrongValue')

                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand('someEnvironment')

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=address=${expectedConsulAddress}"))
            }
        }
    }
}
