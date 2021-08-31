import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.any

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class CredentialsPluginTest {
    @Nested
    public class Init {
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
    public class WithBinding {
        @Test
        void isFluent() {
            def result = CredentialsPlugin.withBinding { }

            assertThat(result, equalTo(CredentialsPlugin))
        }

        @Test
        void addsABinding() {
            def binding = { usernameColonPassword(credentialsId: 'my-user-colon-pass', variable: 'USERPASS') }
            CredentialsPlugin.withBinding(binding)

            def bindings = CredentialsPlugin.getBindings()
            assertThat(bindings, hasItem(binding))
        }

        @Test
        void addsMultipleBindingsIfCalledAgain() {
            def binding1 = { string(credentialsId: 'my-token', variable: 'TOKEN') }
            def binding2 = { usernamePassword(credentialsId: 'my-user-pass', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD') }

            CredentialsPlugin.withBinding(binding1)
            CredentialsPlugin.withBinding(binding2)

            def bindings = CredentialsPlugin.getBindings()
            assertThat(bindings.size(), equalTo(2))
            assertThat(bindings, hasItem(binding1))
            assertThat(bindings, hasItem(binding2))
        }
    }

    // Deprecated: Remove this with Issue #404 and the next major release
    @Nested
    public class WithBuildCredentials {
        @Test
        void addsUsernamePasswordBinding() {
            CredentialsPlugin.withBuildCredentials("credentials1")

            def bindings = CredentialsPlugin.getBindings()
            assertThat(bindings, hasSize(1))
        }

        @Test
        void addsMultipleBindings() {
            CredentialsPlugin.withBuildCredentials("credentials1")
            CredentialsPlugin.withBuildCredentials("credentials2")

            def bindings = CredentialsPlugin.getBindings()
            assertThat(bindings, hasSize(2))
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

    @Nested
    public class AddBuildCredentials {
        @Test
        public void runsTheInnerClosure() {
            def wasRun = false
            def innerClosure = { wasRun = true }
            def plugin = new CredentialsPlugin()

            def addCredentialsClosure = plugin.addBuildCredentials()
            addCredentialsClosure.delegate = new MockWorkflowScript()
            addCredentialsClosure(innerClosure)

            assertThat(wasRun, equalTo(true))
        }

        @Test
        public void callsWithCredentialsOnWorkflowScript() {
            def workflowScript = spy(new MockWorkflowScript())
            def plugin = new CredentialsPlugin()

            def addCredentialsClosure = plugin.addBuildCredentials()
            addCredentialsClosure.delegate = workflowScript
            addCredentialsClosure { }

            verify(workflowScript).withCredentials(any(List), any(Closure))
        }
    }
}

