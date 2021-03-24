import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.equalTo
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
class TerraformTaintPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            TerraformTaintPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformTaintPlugin.class)))
        }

        @Test
        void addsTaintResourceParameter() {
            TerraformTaintPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.StringParameterDefinition',
                name: "TAINT_RESOURCE",
                defaultValue: "",
                description: 'Run `terraform taint` on the resource specified prior to planning and applying.'
            ]))
        }

        @Test
        void addsUntaintResourceParameter() {
            TerraformTaintPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.StringParameterDefinition',
                name: "UNTAINT_RESOURCE",
                defaultValue: "",
                description: 'Run `terraform untaint` on the resource specified prior to planning and applying.'
            ]))
        }
    }

    @Nested
    public class Apply {
        @Test
        void decoratesThePlanCommand() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            plugin.apply(environment)

            verify(environment, times(2)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
        }

        @Test
        void skipsTaintWhenNoResource() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            def command = spy(new TerraformTaintCommand('env'))
            MockJenkinsfile.withEnv([
                'TAINT_RESOURCE': ''
            ])
            plugin.apply(command)

            verify(command, times(0)).withResource()
        }

        @Test
        void setsTaintResource() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            def command = spy(new TerraformTaintCommand('env'))
            MockJenkinsfile.withEnv([
                'TAINT_RESOURCE': 'foo.bar'
            ])
            plugin.apply(command)

            verify(command, times(1)).withResource(eq('foo.bar'))
        }

        @Test
        void skipsUntaintWhenNoResource() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            def command = spy(new TerraformUntaintCommand('env'))
            MockJenkinsfile.withEnv([
                'UNTAINT_RESOURCE': ''
            ])
            plugin.apply(command)

            verify(command, times(0)).withResource()
        }

        @Test
        void setsUntaintResource() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            def command = spy(new TerraformUntaintCommand('env'))
            MockJenkinsfile.withEnv([
                'UNTAINT_RESOURCE': 'foo.bar'
            ])
            plugin.apply(command)

            verify(command, times(1)).withResource(eq('foo.bar'))
        }
    }

    @Nested
    public class ShouldApply {
        @Test
        void returnsTrueWhenNoOriginRepoSet() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            MockJenkinsfile.withEnv(['BRANCH_NAME': 'master', 'GIT_URL': 'https://git.foo/username/repo'])

            def result = plugin.shouldApply()
            assertThat(result, equalTo(true))
        }

        @Test
        void returnsFalseWhenOriginRepoMismatch() {
            TerraformTaintPlugin.onlyOnOriginRepo('username/repo')
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            MockJenkinsfile.withEnv(['BRANCH_NAME': 'master', 'GIT_URL': 'https://git.foo/fork/repo'])

            def result = plugin.shouldApply()
            assertThat(result, equalTo(false))
        }

        @Test
        void returnsTrueWhenOriginRepoMatches() {
            TerraformTaintPlugin.onlyOnOriginRepo('username/repo')
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            MockJenkinsfile.withEnv(['BRANCH_NAME': 'master', 'GIT_URL': 'https://git.foo/username/repo'])

            def result = plugin.shouldApply()
            assertThat(result, equalTo(true))
        }

        @Test
        void returnsFalseWhenWrongBranch() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            MockJenkinsfile.withEnv(['BRANCH_NAME': 'notmaster', 'GIT_URL': 'https://git.foo/username/repo'])

            def result = plugin.shouldApply()
            assertThat(result, equalTo(false))
        }

        @Test
        void usesMasterAsDefaultBranch() {
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            MockJenkinsfile.withEnv(['BRANCH_NAME': 'master', 'GIT_URL': 'https://git.foo/username/repo'])

            def result = plugin.shouldApply()
            assertThat(result, equalTo(true))
        }

        @Test
        void returnsTrueWhenOnCorrectCustomBranch() {
            TerraformTaintPlugin.onBranch("main")
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            MockJenkinsfile.withEnv(['BRANCH_NAME': 'main', 'GIT_URL': 'https://git.foo/username/repo'])

            def result = plugin.shouldApply()
            assertThat(result, equalTo(true))
         }

        @Test
        void worksWithMultipleCustomBranches() {
            TerraformTaintPlugin.onBranch("main").onBranch("notmaster")
            TerraformTaintPlugin plugin = new TerraformTaintPlugin()
            MockJenkinsfile.withEnv(['BRANCH_NAME': 'notmaster', 'GIT_URL': 'https://git.foo/username/repo'])

            def result = plugin.shouldApply()
            assertThat(result, equalTo(true))
         }
    }
}
