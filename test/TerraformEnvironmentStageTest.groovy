import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.instanceOf
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat
import static TerraformEnvironmentStage.PLAN

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformEnvironmentStageTest {
    @Nested
    public class ToString {
        @Test
        void returnsEnvironmentName() {
            def expectedEnvironment = 'foo'
            def stage = new TerraformEnvironmentStage(expectedEnvironment)

            def result = stage.toString()

            assertThat(result, equalTo(expectedEnvironment))
        }
    }

    @Nested
    public class Then {

        @Test
        void nextStageisCalled() {
            def stage  = new TerraformEnvironmentStage('foo')
            def stage2 = new TerraformEnvironmentStage('foo2')

            def result = stage.then(stage2)

            assertThat(result, instanceOf(BuildGraph.class))
        }
    }

    @Nested
    public class AddedPlugins {
        @Test
        void willHaveApplyCalled() {
            TerraformEnvironmentStagePlugin fakePlugin = mock(TerraformEnvironmentStagePlugin.class)
            TerraformEnvironmentStage.addPlugin(fakePlugin)

            TerraformEnvironmentStage stage = new TerraformEnvironmentStage('anyStage')
            stage.applyPlugins()

            verify(fakePlugin).apply(stage)
        }
    }

    @Nested
    public class WithEnv {
        @Test
        void addsAnInstanceOfEnvironmentVariablePlugin() {
            def stage = new TerraformEnvironmentStage('foo')
            stage.withEnv('localKey', 'localValue')

            def plugins = stage.getAllPlugins()

            assertThat(plugins, hasItem(instanceOf(EnvironmentVariablePlugin.class)))
        }

        @Test
        void preservesOrderOfOtherPlugins() {
            def stage = new TerraformEnvironmentStage('foo')
            def plugin1 = mock(TerraformEnvironmentStagePlugin.class)
            def plugin3 = mock(TerraformEnvironmentStagePlugin.class)

            stage.addPlugin(plugin1)
            stage.withEnv('someKey', 'someValue')
            stage.addPlugin(plugin3)

            def plugins = stage.getAllPlugins()

            def plugin1Index = plugins.findIndexOf { it == plugin1 }

            assertThat(plugins[plugin1Index], is(plugin1))
            assertThat(plugins[plugin1Index + 1], is(instanceOf(EnvironmentVariablePlugin.class)))
            assertThat(plugins[plugin1Index + 2], is(plugin3))
        }

        @Test
        void preservesMultipleEnvironmentPlugins() {
            def stage = new TerraformEnvironmentStage('foo')

            stage.withEnv('key1', 'value1')
                 .withEnv('key2', 'value2')

            def plugins = stage.getAllPlugins()
                               .findAll { plugin -> plugin instanceof EnvironmentVariablePlugin }

            assertThat(plugins.size(), equalTo(2))
        }

        @Test
        void doesNotAddPluginToOtherInstances() {
            def modifiedStage = new TerraformEnvironmentStage('modified')
            def unmodifiedStage = new TerraformEnvironmentStage('unmodified')

            def pluginsBefore = unmodifiedStage.getAllPlugins()

            modifiedStage.withEnv('somekey', 'somevalue')

            def pluginsAfter = unmodifiedStage.getAllPlugins()

            assertThat(pluginsAfter, equalTo(pluginsBefore))
        }

        @Test
        void isFluent() {
            def stage = new TerraformEnvironmentStage('foo')
            def result = stage.withEnv('somekey', 'somevalue')

            assertThat(result, equalTo(stage))
        }
    }

    @Nested
    public class WithGlobalEnv {
        @Test
        void addsAnInstanceOfEnvironmeentVariablePlugin() {
            TerraformEnvironmentStage.withGlobalEnv('globalKey', 'globalValue')

            def plugins = TerraformEnvironmentStage.getPlugins()

            assertThat(plugins, hasItem(is(instanceOf(EnvironmentVariablePlugin.class))))
        }

        @Test
        void isFluent() {
            def result = TerraformEnvironmentStage.withGlobalEnv('somekey', 'somevalue')

            assertThat(result, equalTo(TerraformEnvironmentStage.class))
        }
    }

    @Nested
    class PipelineConfigurations {
        @Test
        void returnsAClosure() {
            def stage = new TerraformEnvironmentStage('foo')

            def result = stage.pipelineConfiguration()

            assertThat(result, is(instanceOf(Closure.class)))
        }

        @Test
        void doesNotBlowUpWhenRunningClosure() {
            MockJenkinsfile.withEnv()
            Jenkinsfile.defaultNodeName = 'foo'
            def stage = new TerraformEnvironmentStage('foo')

            def closure = stage.pipelineConfiguration()
            closure.delegate = new DummyJenkinsfile()
            closure()
        }
    }

    @Nested
    class WithStageNamePattern {
        @Test
        void constructsTheDefaultStageNameWhenBlank() {
            def stage = new TerraformEnvironmentStage('myenv')

            def actualName = stage.getStageNameFor(PLAN)

            assertThat(actualName, equalTo('plan-myenv'))
        }

        @Test
        void constructTheStageNameUsingTheGivenPattern() {
            TerraformEnvironmentStage.withStageNamePattern { options -> "${options['command']}-override-${options['environment']}" }
            def stage = new TerraformEnvironmentStage('myenv')

            def actualName = stage.getStageNameFor(PLAN)

            assertThat(actualName, equalTo('plan-override-myenv'))
        }
    }
}
