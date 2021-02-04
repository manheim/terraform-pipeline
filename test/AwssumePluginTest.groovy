import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class AwssumePluginTest {
    @BeforeEach
    void resetJenkinsEnv() {
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
        void modifiesTerraformInitCommand() {
            AwssumePlugin.init()

            Collection actualPlugins = TerraformInitCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(AwssumePlugin.class)))
        }

        @Test
        void modifiesTerraformPlanCommand() {
            AwssumePlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(AwssumePlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            AwssumePlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(AwssumePlugin.class)))
        }
    }

    @Nested
    public class Apply {

        @Nested
        public class WithRoleProvided {
            @Test
            void addsEnvironmentSpecificRoleArnAsPrefixToTerraformInit() {
                String environment = "myEnv"
                configureJenkins(env: ['MYENV_AWS_ROLE_ARN': 'someArn'])
                AwssumePlugin plugin = new AwssumePlugin()
                TerraformInitCommand command = new TerraformInitCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("AWS_ROLE_ARN=someArn awssume terraform init"))
            }

            @Test
            void addsEnvironmentSpecificRoleArnAsPrefixToTerraformPlan() {
                String environment = "myEnv"
                configureJenkins(env: ['MYENV_AWS_ROLE_ARN': 'someArn'])
                AwssumePlugin plugin = new AwssumePlugin()
                TerraformPlanCommand command = new TerraformPlanCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("AWS_ROLE_ARN=someArn awssume terraform plan"))
            }

            @Test
            void addsEnvironmentSpecificRoleArnAsPrefixToTerraformApply() {
                String environment = "myEnv"
                configureJenkins(env: ['MYENV_AWS_ROLE_ARN': 'someArn'])
                AwssumePlugin plugin = new AwssumePlugin()
                TerraformApplyCommand command = new TerraformApplyCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("AWS_ROLE_ARN=someArn awssume terraform apply"))
            }
        }

        @Nested
        public class WithoutRoleProvided {
            @Test
            void skipsAwssumeForTerraformInit() {
                String environment = "myEnv"
                configureJenkins(env: [:])
                AwssumePlugin plugin = new AwssumePlugin()
                TerraformInitCommand command = new TerraformInitCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, not(containsString("awssume")))
                assertThat(result, containsString("terraform init"))
            }

            @Test
            void skipsAwssumeForTerraformPlan() {
                String environment = "myEnv"
                configureJenkins(env: [:])
                AwssumePlugin plugin = new AwssumePlugin()
                TerraformPlanCommand command = new TerraformPlanCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, not(containsString("awssume")))
                assertThat(result, containsString("terraform plan"))
            }

            @Test
            void skipsAwssumeForTerraformApply() {
                String environment = "myEnv"
                configureJenkins(env: [:])
                AwssumePlugin plugin = new AwssumePlugin()
                TerraformApplyCommand command = new TerraformApplyCommand(environment)

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, not(containsString("awssume")))
                assertThat(result, containsString("terraform apply"))
            }
        }
    }

    @Nested
    public class GetAwsRoleArn {
        @Test
        void returnsAwsRoleArnIfPresent() {
            String expectedRole = 'someRoleArn'
            configureJenkins(env: ['AWS_ROLE_ARN': expectedRole])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRole = plugin.getAwsRoleArn('myenv')

            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void returnsEnvironmentSpecificAwsRoleArnIfPresent() {
            String expectedRole = 'myenvRole'
            configureJenkins(env: ['MYENV_AWS_ROLE_ARN': expectedRole])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRole = plugin.getAwsRoleArn('myenv')

            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void returnsEnvironmentSpecifiedAwsRoleArnIfPresentCaseInsensitive() {
            String expectedRole = 'myenvRole'
            configureJenkins(env: ['myenv_AWS_ROLE_ARN': expectedRole])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRole = plugin.getAwsRoleArn('myenv')

            assertThat(actualRole, is(expectedRole))
        }

        @Test
        void prefersAwsArnRoleOverEnvironmentSpecificRole() {
            String expectedRole = 'thisRole'
            configureJenkins(env: [
                'AWS_ROLE_ARN': expectedRole,
                'myenv_AWS_ROLE_ARN': 'notThisRole'
            ])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRole = plugin.getAwsRoleArn('myenv')

            assertThat(actualRole, is(expectedRole))
        }
    }

    @Nested
    public class GetRegion {
        @Test
        void returnsAwsRegionIfPresent() {
            String expectedRegion = 'someRegion'
            configureJenkins(env: ['AWS_REGION': expectedRegion])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void returnsEnvironmentSpecificRegionIfPresent() {
            String expectedRegion = 'environmentSpecificRegion'
            configureJenkins(env: ['MYENV_AWS_REGION': expectedRegion])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void returnsAwsDefaultRegionIfPresent() {
            String expectedRegion = 'defaultRegion'
            configureJenkins(env: ['AWS_DEFAULT_REGION': expectedRegion])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void prefersRegionOverEnvironmentSpecificRegion() {
            String expectedRegion = 'thisRegion'
            configureJenkins(env: [
                'AWS_REGION': expectedRegion,
                'MYENV_AWS_REGION': 'notThisRegion'
            ])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void prefersEnvironmentSpecificRegionOverDefaultRegion() {
            String expectedRegion = 'thisRegion'
            configureJenkins(env: [
                'MYENV_AWS_REGION': expectedRegion,
                'AWS_DEFAULT_REGION': 'notThisRegion'
            ])

            AwssumePlugin plugin = new AwssumePlugin()
            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }
    }
}
