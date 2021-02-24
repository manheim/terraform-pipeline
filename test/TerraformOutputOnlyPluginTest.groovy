import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformOutputOnlyPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            TerraformOutputOnlyPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformOutputOnlyPlugin.class)))
        }

        @Test
        void addsParameters() {
            TerraformOutputOnlyPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.BooleanParameterDefinition',
                name: "SHOW_OUTPUTS_ONLY",
                defaultValue: false,
                description: "Only run 'terraform output' to show outputs, skipping plan and apply."
            ]))

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.BooleanParameterDefinition',
                name: "JSON_FORMAT_OUTPUTS",
                defaultValue: false,
                description: "Render 'terraform output' results as JSON. Only applies if SHOW_OUTPUTS_ONLY is selected."
            ]))

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.StringParameterDefinition',
                name: "REDIRECT_OUTPUTS_TO_FILE",
                defaultValue: "",
                description: "Filename relative to the current workspace to redirect output to. Only applies if 'SHOW_OUTPUTS_ONLY' is selected."
            ]))
        }
    }

    @Nested
    public class Apply {
        @Test
        void doesNotDecorateTheTerraformEnvironmentStageIfFalse() {
            TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            MockJenkinsfile.withEnv('SHOW_OUTPUTS_ONLY': 'false')
            plugin.apply(environment)

            verify(environment, times(0)).decorate(eq(TerraformEnvironmentStage.INIT_COMMAND), any(Closure.class))
            verify(environment, times(0)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
            verify(environment, times(0)).decorateAround(eq(TerraformEnvironmentStage.CONFIRM), any(Closure.class))
            verify(environment, times(0)).decorateAround(eq(TerraformEnvironmentStage.APPLY), any(Closure.class))
        }

        @Test
        void decoratesTheTerraformEnvironmentStageIfTrue() {
            TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            MockJenkinsfile.withEnv('SHOW_OUTPUTS_ONLY': 'true')
            plugin.apply(environment)

            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.INIT_COMMAND), any(Closure.class))
            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
            verify(environment, times(1)).decorateAround(eq(TerraformEnvironmentStage.CONFIRM), any(Closure.class))
            verify(environment, times(1)).decorateAround(eq(TerraformEnvironmentStage.APPLY), any(Closure.class))
        }

        @Test
        void skipsJsonArgumentIfFalse() {
            TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()
            def command = new TerraformOutputCommand()
            MockJenkinsfile.withEnv([
                'SHOW_OUTPUTS_ONLY': 'true',
                'JSON_FORMAT_OUTPUTS': 'false'
            ])
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-json")))
        }

        @Test
        void addsJsonArgumentIfTrue() {
            TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()
            def command = new TerraformOutputCommand()
            MockJenkinsfile.withEnv([
                'SHOW_OUTPUTS_ONLY': 'true',
                'JSON_FORMAT_OUTPUTS': 'true'
            ])
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-json"))
        }

        @Test
        void skipsRedirectArgumentIfEmpty() {
            TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()
            def command = new TerraformOutputCommand()
            MockJenkinsfile.withEnv([
                'SHOW_OUTPUTS_ONLY': 'true',
                'REDIRECT_OUTPUTS_TO_FILE': ''
            ])
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString(">")))
        }

        @Test
        void addsRedirectArgumentIfNotEmpty() {
            TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()
            def command = new TerraformOutputCommand()
            MockJenkinsfile.withEnv([
                'SHOW_OUTPUTS_ONLY': 'true',
                'REDIRECT_OUTPUTS_TO_FILE': 'foo'
            ])
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString(">foo"))
        }
    }
}
