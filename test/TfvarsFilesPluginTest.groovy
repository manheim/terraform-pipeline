import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
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

    static void initWithGlobalFile(String file) {
        TfvarsFilesPlugin.withGlobalVarFile(file).init()
    }

    @Nested
    class Init {
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

    @Nested
    class Directory {
        @Test
        void setsDirectory() {
            setupOriginalContext()
            initWithDir('./test/resources')
            assertThat(TfvarsFilesPlugin.directory, equalTo('./test/resources'))
        }
    }

    @Nested
    class ApplyCommand {
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
        void addsGlobalFileIfFileExists() {
            fileWillExist()
            initWithGlobalFile("globals.tfvars")
            def command = TerraformApplyCommand.instanceFor('test')
            assertThat(command.toString(), containsString('-var-file=./globals.tfvars'))
        }

        @Test
        void doesNotAddGlobalFileIfFileDoesntExist() {
            fileWillNotExist()
            initWithGlobalFile("globals.tfvars")
            def command = TerraformApplyCommand.instanceFor('test')
            assertThat(command.toString(), not(containsString('-var-file=./globals.tfvars')))
        }

        @Test
        void globalRespectsDirectorySetting() {
            fileWillExist()
            TfvarsFilesPlugin.withDirectory("./resources").withGlobalVarFile('globals.tfvars').init()
            def command = TerraformApplyCommand.instanceFor('test')
            assertThat(command.toString(), containsString('-var-file=./resources/globals.tfvars'))
        }

        @Test
        void respectsDirectorySetting() {
            fileWillExist()
            initWithDir('./resources')

            def command = TerraformApplyCommand.instanceFor('test')
            assertThat(command.toString(), containsString('-var-file=./resources/test.tfvars'))
        }
    }

    @Nested
    class PlanCommand {
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
        void addsGlobalFileIfFileExists() {
            fileWillExist()
            initWithGlobalFile("globals.tfvars")
            def command = TerraformPlanCommand.instanceFor('test')
            assertThat(command.toString(), containsString('-var-file=./globals.tfvars'))
        }

        @Test
        void doesNotAddGlobalFileIfFileDoesntExist() {
            fileWillNotExist()
            initWithGlobalFile("globals.tfvars")
            def command = TerraformPlanCommand.instanceFor('test')
            assertThat(command.toString(), not(containsString('-var-file=./globals.tfvars')))
        }

        @Test
        void globalRespectsDirectorySetting() {
            fileWillExist()
            TfvarsFilesPlugin.withDirectory("./resources").withGlobalVarFile('globals.tfvars').init()
            def command = TerraformPlanCommand.instanceFor('test')
            assertThat(command.toString(), containsString('-var-file=./resources/globals.tfvars'))
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

