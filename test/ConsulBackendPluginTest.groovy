import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class ConsulBackendPluginTest {
    public class Init {
        @After
        void resetPlugins() {
            TerraformInitCommand.resetPlugins()
        }

        @Test
        void modifiesTerraformInitCommand() {
            ConsulBackendPlugin.init()

            Collection actualPlugins = TerraformInitCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(ConsulBackendPlugin.class)))
        }
    }

    public class Apply {
        @Before
        public void resetJenkinsfile() {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            when(Jenkinsfile.instance.getEnv()).thenReturn([:])
        }

        private configureJenkins(Map config = [:]) {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
            when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
        }

        public class PathBackendParameter {
            @Test
            void isAddedAndIsEnvironmentSpecific() {
                String repoSlug = 'myOrg/myRepo'
                configureJenkins(repoSlug: repoSlug)

                String environment = "myEnv"
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=path=terraform/${repoSlug}_${environment}"))
            }

            @Test
            void isAddedAndUsesCustomizablePattern() {
                configureJenkins(repoSlug: 'someOrg/someRepo')

                ConsulBackendPlugin.pathPattern = { String env -> "customPatternFor_${env}" }
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=path=customPatternFor_myEnv"))
            }
        }

        public class AddressBackendParameter {
            @Test
            void isNotAddedByDefault() {
                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand('someEnvironment')

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, not(containsString("-backend-config=address")))
            }

            @Test
            void isAddedIfEnvironmentVariablePresent() {
                String expectedConsulAddress = 'someAddress'
                configureJenkins(env: [ DEFAULT_CONSUL_ADDRESS: expectedConsulAddress ])
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
                configureJenkins(env: [ DEFAULT_CONSUL_ADDRESS: 'theWrongValue' ])

                ConsulBackendPlugin plugin = new ConsulBackendPlugin()
                TerraformInitCommand command = new TerraformInitCommand('someEnvironment')

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("-backend-config=address=${expectedConsulAddress}"))
            }
        }
    }
}
