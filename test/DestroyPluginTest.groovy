import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class DestroyPluginTest {
    @Before
    @After
    void reset() {
        Jenkinsfile.reset()
        ConfirmApplyPlugin.reset()
        TerraformEnvironmentStage.reset()
        TerraformPlanCommand.resetPlugins()
        TerraformApplyCommand.resetPlugins()
    }

    public class Init {
        @Before
        void setup() {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            doReturn('repoName').when(Jenkinsfile.instance).getRepoName()
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            DestroyPlugin.init()
            def stage = new TerraformEnvironmentStage('foo')

            def actualStageName = stage.getStageNameFor(TerraformEnvironmentStage.PLAN)

            assertThat(actualStageName, containsString('DESTROY'))
        }

        @Test
        void modifiesConfirmApplyPlugin() {
            DestroyPlugin.init()

            String confirmMessage = ConfirmApplyPlugin.confirmMessage
            String okMessage = ConfirmApplyPlugin.okMessage

            assertEquals(DestroyPlugin.DESTROY_CONFIRM_MESSAGE, confirmMessage)
            assertEquals(DestroyPlugin.DESTROY_OK_MESSAGE, okMessage)
        }

        @Test
        void modifiesTerraformPlanCommand() {
            DestroyPlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(DestroyPlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            DestroyPlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(DestroyPlugin.class)))
        }
    }

    class ApplyTerraformPlanCommand {
        @Test
        void modifiesPlanToIncludeDestroyArgument() {
            def command = new TerraformPlanCommand()
            def plugin = new DestroyPlugin()

            plugin.apply(command)
            def result = command.toString()

            assertThat(result, containsString('-destroy'))
        }
    }

    class ApplyTerraformApplyCommand {
        @Test
        void modifiesCommandFromApplyToDestroy() {
            def command = new TerraformApplyCommand()
            def plugin = new DestroyPlugin()

            plugin.apply(command)
            def result = command.toString()

            assertThat(result, containsString('terraform destroy'))
        }

        class WithArguments {
            @After
            void resetPlugins() {
                DestroyPlugin.reset()
            }

            @Test
            void includesTheGivenArgument() {
                def expectedArgument = '-refresh=false'
                def command = new TerraformApplyCommand()
                def plugin = new DestroyPlugin()

                plugin.withArgument(expectedArgument)
                plugin.apply(command)
                def result = command.toString()

                assertThat(result, containsString(expectedArgument))
            }

            @Test
            void includesMultipleArguments() {
                def arg1 = '-arg1'
                def arg2 = '-arg2'
                def command = new TerraformApplyCommand()
                def plugin = new DestroyPlugin()

                plugin.withArgument(arg1)
                plugin.withArgument(arg2)
                plugin.apply(command)
                def result = command.toString()

                assertThat(result, containsString(arg1))
                assertThat(result, containsString(arg2))
            }
        }
    }

    class WithArgument {
        @Test
        void isFluent() {
            def result = DestroyPlugin.withArgument('-arg1')

            assertEquals(DestroyPlugin.class, result)
        }
    }

    class ConfirmCondition {
        @Test
        void returnsTrueIfConfirmationInputDoesNotMatch() {
            def condition = DestroyPlugin.getConfirmCondition('myApp')

            assertTrue(condition.call(['environment': 'foo', 'input': ['CONFIRM_DESTROY': 'destroy myApp foo']]))
        }

        @Test
        void returnsFalseIfConfirmationInputDoesNotMatch() {
            def condition = DestroyPlugin.getConfirmCondition('myApp')

            assertFalse(condition.call(['environment': 'foo', 'input': ['CONFIRM_DESTROY': 'anythingElse']]))
        }
    }
}
