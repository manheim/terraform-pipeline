import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*


@RunWith(HierarchicalContextRunner.class)
class TerraformPluginTest {

    @Before
    void mocksAndResets() {
        Jenkinsfile.instance.original = new Expando()
        TerraformPlugin.version = null
    }

    void setupForFileExists() {
        Jenkinsfile.instance.original.fileExists = { file -> true}
        Jenkinsfile.instance.original.readFile = { file -> '0.12.0-foobar'}
    }

    void setupForFileDoesNotExist() {
        Jenkinsfile.instance.original.fileExists = { file -> false}
    }

    class VersionDetection {

        @Test
        void usesDefaultIfNoFilePresent() {
            setupForFileDoesNotExist()
            TerraformPlugin.detectVersion()
            assertEquals(TerraformPlugin.DEFAULT_VERSION, TerraformPlugin.version.version)
        }

        @Test
        void usesFileIfPresent() {
            setupForFileExists()
            TerraformPlugin.detectVersion()
            assertEquals('0.12.0-foobar', TerraformPlugin.version.version)
        }
    }

    class WithVersion {
        @Test
        void usesVersionEvenIfFileExists() {
            setupForFileExists()
            TerraformPlugin.withVersion('2.0.0')
            assertEquals('2.0.0', TerraformPlugin.version.version)
        }
    }

    class ValidateCommandModifications {
        @Before
        void setup() {
            setupForFileDoesNotExist()
        }

        @Test
        void lessThan_0_12_0_HasCheckVariablesFalse() {
            TerraformPlugin.withVersion('0.11.14')
            def command = TerraformValidateCommand.instance()
            assertThat(command.toString(), containsString(' -check-variables=false'))
        }

        @Test
        void equalTo_0_12_0_DoesNotHaveCheckVariables() {
            TerraformPlugin.withVersion('0.12.0')
            def command = TerraformValidateCommand.instance()
            assertThat(command.toString(), not(containsString(' -check-variables=false')))
        }

        @Test
        void greaterThan_0_12_0_DoesNotHaveCheckVaraibles() {
            TerraformPlugin.withVersion('0.12.3')
            def command = TerraformValidateCommand.instance()
            assertThat(command.toString(), not(containsString(' -check-variables=false')))
        }
    }
}
