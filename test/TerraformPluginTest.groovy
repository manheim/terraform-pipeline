import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.*

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
}
