import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
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
class TerraformImportPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            TerraformImportPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformImportPlugin.class)))
        }

        @Test
        void addsImportResourceParameter() {
            TerraformImportPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.StringParameterDefinition',
                name: "IMPORT_RESOURCE",
                defaultValue: "",
                description: "Run `terraform import` on the resource specified prior to planning and applying."
            ]))
        }

        @Test
        void addsImportTargetPathParameter() {
            TerraformImportPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.StringParameterDefinition',
                name: "IMPORT_TARGET_PATH",
                defaultValue: "",
                description: "The path in the Terraform state to import the spcified resource to."
            ]))
        }

        @Test
        void addsImportEnvironmentParameter() {
            TerraformImportPlugin.init()

            def parametersPlugin = new BuildWithParametersPlugin()
            Collection actualParms = parametersPlugin.getBuildParameters()

            assertThat(actualParms, hasItem([
                $class: 'hudson.model.StringParameterDefinition',
                name: "IMPORT_ENVIRONMENT",
                defaultValue: "",
                description: "The environment in which to run the import."
            ]))
        }
    }

    @Nested
    public class Apply {
        @Test
        void doesNotDecorateTheTerraformEnvironmentStageIfNoResource() {
            TerraformImportPlugin plugin = new TerraformImportPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            MockJenkinsfile.withEnv([
                'IMPORT_TARGET_PATH': 'target.foo'
            ])
            plugin.apply(environment)

            verify(environment, times(0)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
        }

        @Test
        void doesNotDecorateTheTerraformEnvironmentStageIfNoTargetPath() {
            TerraformImportPlugin plugin = new TerraformImportPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            MockJenkinsfile.withEnv([
                'IMPORT_RESOURCE': 'foo'
            ])
            plugin.apply(environment)

            verify(environment, times(0)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
        }

        @Test
        void doesNotDecorateTheTerraformEnvironmentStageIfNoEnvironment() {
            TerraformImportPlugin plugin = new TerraformImportPlugin()
            def environment = spy(new TerraformEnvironmentStage('foobar'))
            MockJenkinsfile.withEnv([
                'IMPORT_RESOURCE': 'foo',
                'IMPORT_TARGET_PATH': 'foo'
            ])
            plugin.apply(environment)

            verify(environment, times(0)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
        }

        @Test
        void doesNotDecorateTheTerraformEnvironmentStageIfEnvironmentMismatch() {
            TerraformImportPlugin plugin = new TerraformImportPlugin()
            def environment = spy(new TerraformEnvironmentStage('foobar'))
            MockJenkinsfile.withEnv([
                'IMPORT_RESOURCE': 'foo',
                'IMPORT_TARGET_PATH': 'foo',
                'IMPORT_ENVIRONMENT': 'barbaz'
            ])
            plugin.apply(environment)

            verify(environment, times(0)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
        }

        @Test
        void decoratesTheTerraformEnvironmentStageIfResourceEnvironmentAndTargetSet() {
            TerraformImportPlugin plugin = new TerraformImportPlugin()
            def environment = spy(new TerraformEnvironmentStage('foobar'))
            MockJenkinsfile.withEnv([
                'IMPORT_RESOURCE': 'foo',
                'IMPORT_TARGET_PATH': 'target.foo',
                'IMPORT_ENVIRONMENT': 'foobar'
            ])
            plugin.apply(environment)

            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
        }
    }
}
