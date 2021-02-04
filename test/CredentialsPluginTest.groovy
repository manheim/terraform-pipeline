import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.any

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CredentialsPluginTest {
    @Nested
    public class Init {
        @AfterEach
        void resetPlugins() {
            BuildStage.resetPlugins()
            RegressionStage.resetPlugins()
            TerraformEnvironmentStage.reset()
            TerraformValidateStage.reset()
            CredentialsPlugin.reset()
        }

        @Test
        void modifiesBuildStage() {
            CredentialsPlugin.init()

            Collection actualPlugins = BuildStage.getPlugins()

            assertThat(actualPlugins, hasItem(instanceOf(CredentialsPlugin.class)))
        }

        @Test
        void modifiesRegressionStage() {
            CredentialsPlugin.init()

            Collection actualPlugins = RegressionStage.getPlugins()

            assertThat(actualPlugins, hasItem(instanceOf(CredentialsPlugin.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStage() {
            CredentialsPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()

            assertThat(actualPlugins, hasItem(instanceOf(CredentialsPlugin.class)))
        }
    }

    @Nested
    public class WithBuildCredentials {
        @AfterEach
        void resetPlugin() {
            CredentialsPlugin.reset()
        }

        @Test
        void addsCredentialsForBuildStage() {
            CredentialsPlugin.withBuildCredentials("credentials1")

            def buildCredentials = CredentialsPlugin.getBuildCredentials()
            assertThat(buildCredentials, hasSize(1))

            def credential = buildCredentials.find { it['credentialsId'] == "credentials1" }
            assertThat(credential, notNullValue())
        }

        @Test
        void addsMultipleCredentialsForBuildStage() {
            CredentialsPlugin.withBuildCredentials("credentials1")
            CredentialsPlugin.withBuildCredentials("credentials2")

            def buildCredentials = CredentialsPlugin.getBuildCredentials()
            assertThat(buildCredentials, hasSize(2))

            def credential1 = buildCredentials.find { it['credentialsId'] == "credentials1" }
            assertThat(credential1, notNullValue())
            def credential2 = buildCredentials.find { it['credentialsId'] == "credentials2" }
            assertThat(credential2, notNullValue())
        }
    }

    @Nested
    public class ToEnvironmentVariable {
        @Test
        void convertsLowercaseToUppercase() {
            String lower = "mYvar"

            String result = CredentialsPlugin.toEnvironmentVariable(lower)

            assertThat(result, is(equalTo("MYVAR")))
        }

        @Test
        void convertsDashesToUnderscore() {
            String withDash = "MY-VAR"

            String result = CredentialsPlugin.toEnvironmentVariable(withDash)

            assertThat(result, is(equalTo("MY_VAR")))
        }

        @Test
        void convertsAllTheThings() {
            String withAllTheThings = "my-Var"

            String result = CredentialsPlugin.toEnvironmentVariable(withAllTheThings)

            assertThat(result, is(equalTo("MY_VAR")))
        }
    }

    @Nested
    public class PopulateDefaults {
        @Test
        void populatesCredentialsId() {
            String credentialsId = 'myId'

            Map results = CredentialsPlugin.populateDefaults(credentialsId)

            assertThat(results['credentialsId'], is(equalTo(credentialsId)))
        }

        @Test
        void defaultsUserVariableUsingCredentialsId() {
            String credentialsId = 'myId'

            Map results = CredentialsPlugin.populateDefaults(credentialsId)

            assertThat(results['usernameVariable'], is(equalTo("MYID_USERNAME")))
        }

        @Test
        void defaultsPasswordVariableUsingCredentialsId() {
            String credentialsId = 'myId'

            Map results = CredentialsPlugin.populateDefaults(credentialsId)

            assertThat(results['passwordVariable'], is(equalTo("MYID_PASSWORD")))
        }

        @Test
        void allowsCustomUserVariable() {
            String credentialsId = 'myId'
            String customUserVariable = "MY_CUSTOM_USERNAME_VARIABLE"

            Map results = CredentialsPlugin.populateDefaults(credentialsId, usernameVariable: customUserVariable)

            assertThat(results['usernameVariable'], is(equalTo(customUserVariable)))
        }

        @Test
        void allowsCustomPasswordVariable() {
            String credentialsId = 'myId'
            String customPasswordVariable = "MY_CUSTOM_PASSWORD_VARIABLE"

            Map results = CredentialsPlugin.populateDefaults(credentialsId, passwordVariable: customPasswordVariable)

            assertThat(results['passwordVariable'], is(equalTo(customPasswordVariable)))
        }
    }

    @Nested
    class Apply {
        @Test
        void decoratesTheBuildStage()  {
            def buildStage = mock(BuildStage.class)
            def plugin = spy(new CredentialsPlugin())

            plugin.apply(buildStage)

            verify(buildStage).decorate(any(Closure.class))
        }

        @Test
        void decoratesTheRegressionStage()  {
            def testStage = mock(RegressionStage.class)
            def plugin = spy(new CredentialsPlugin())

            plugin.apply(testStage)

            verify(testStage).decorate(any(Closure.class))
        }

        @Test
        void decoratesTheTerraformEnvironmentStage()  {
            def environment = mock(TerraformEnvironmentStage.class)
            def plugin = spy(new CredentialsPlugin())

            plugin.apply(environment)

            verify(environment).decorate(any(Closure.class))
        }

        @Test
        void decoratesTheTerraformValidateStage()  {
            def environment = mock(TerraformValidateStage.class)
            def plugin = spy(new CredentialsPlugin())

            plugin.apply(environment)

            verify(environment).decorate(any(Closure.class))
        }
    }
}

