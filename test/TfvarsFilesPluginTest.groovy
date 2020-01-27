import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat

import de.bechte.junit.runners.context.HierarchicalContextRunner
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(HierarchicalContextRunner.class)
class TfvarsFilesPluginTest {

    static void setupOriginalContext() {
        Jenkinsfile.instance = new Jenkinsfile()
        Jenkinsfile.instance.original = new Expando()
    }

    static void fileWillExist() {
        setupOriginalContext()
        Jenkinsfile.instance.original.fileExists = { file -> true }
    }

    static void fileWillNotExist() {
        setupOriginalContext()
        Jenkinsfile.instance.original.fileExists = { file -> false }
        Jenkinsfile.instance.original.echo = { message -> }
    }

    static void initPlugin() {
        TfvarsFilesPlugin.init()
    }

    static void initWithDir(String directory) {
        TfvarsFilesPlugin.withDirectory(directory).init()
    }

    static void reset() {
        TerraformPlanCommand.resetPlugins()
        TerraformApplyCommand.resetPlugins()
        TfvarsFilesPlugin.directory = '.'
    }


    class Init {
        @After
        void resetPlugins() {
            reset()
        }

        @Test
        void modifiesTerraformPlanCommand() {
            setupOriginalContext()
            initPlugin()
            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TfvarsFilesPlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            setupOriginalContext()
            initPlugin()
            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TfvarsFilesPlugin.class)))
        }
    }

    class Directory {
        @After
        void resetPlugins() {
            reset()
        }

        @Test
        void setsDirectory() {
            setupOriginalContext()
            initWithDir('./test/resources')
            assertThat(TfvarsFilesPlugin.directory, equalTo('./test/resources'))
        }
    }

    class ApplyCommand {

        @After
        void resetPlugins() {
            reset()
        }

        @Test
        void returnsArgumentIfFileExists() {
            fileWillExist()
            initPlugin()

            def command = TerraformApplyCommand.instanceFor('test')
            assertThat(command.toString(), containsString('-var-file=./test.tfvars'))
        }

        @Test
        void doesNotAddArgIfFileDoesntExist() {
            fileWillNotExist()
            initPlugin()

            def command = TerraformApplyCommand.instanceFor('test')
            assertThat(command.toString(), not(containsString('-var-file')))
        }

        @Test
        void respectsDirectorySetting() {
            fileWillExist()
            initWithDir('./resources')

            def command = TerraformApplyCommand.instanceFor('test')
            assertThat(command.toString(), containsString('-var-file=./resources/test.tfvars'))
        }

        class PlanCommand {

            @After
            void resetPlugins() {
                reset()
            }

            @Test
            void returnsArgumentIfFileExists() {
                fileWillExist()
                initPlugin()

                def command = TerraformPlanCommand.instanceFor('test')
                assertThat(command.toString(), containsString('-var-file=./test.tfvars'))
            }

            @Test
            void doesNotAddArgIfFileDoesntExists() {
                fileWillNotExist()
                initPlugin()

                def command = TerraformPlanCommand.instanceFor('test')
                assertThat(command.toString(), not(containsString('-var-file')))
            }

            @Test
            void respectsDirectorySetting() {
                fileWillExist()
                initWithDir('./resources')

                def command = TerraformPlanCommand.instanceFor('test')
                assertThat(command.toString(), containsString('-var-file=./resources/test.tfvars'))
            }
        }
    }
}

