import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformPluginTest {

    class VersionDetection {
        @After
        void resetVersion() {
            TerraformPlugin.resetVersion()
        }

        @Test
        void usesDefaultIfNoFilePresent() {
            def plugin = spy(new TerraformPlugin())
            doReturn(false).when(plugin).fileExists(TerraformPlugin.TERRAFORM_VERSION_FILE)

            def foundVersion = plugin.detectVersion()

            assertEquals(TerraformPlugin.DEFAULT_VERSION, foundVersion)
        }

        @Test
        void usesFileIfPresent() {
            def expectedVersion =  '0.12.0-foobar'
            def plugin = spy(new TerraformPlugin())
            doReturn(true).when(plugin).fileExists(TerraformPlugin.TERRAFORM_VERSION_FILE)
            doReturn(expectedVersion).when(plugin).readFile(TerraformPlugin.TERRAFORM_VERSION_FILE)

            def foundVersion = plugin.detectVersion()

            assertEquals(expectedVersion, foundVersion)
        }
    }

    class WithVersion {
        @After
        void resetVersion() {
            TerraformPlugin.resetVersion()
        }

        @Test
        void usesVersionEvenIfFileExists() {
            TerraformPlugin.withVersion('2.0.0')
            assertEquals('2.0.0', TerraformPlugin.version)
        }
    }

    class Strategyfor {
        @After
        void resetVersion() {
            TerraformPlugin.resetVersion()
        }

        @Test
        void returnsVersion11ForLessThan0_12_0() {
            def plugin = new TerraformPlugin()

            def foundStrategy = plugin.strategyFor('0.11.3')

            assertThat(foundStrategy, instanceOf(TerraformPluginVersion11.class))
        }

        @Test
        void returnsVersion12For0_12_0() {
            def plugin = new TerraformPlugin()

            def foundStrategy = plugin.strategyFor('0.12.0')

            assertThat(foundStrategy, instanceOf(TerraformPluginVersion12.class))
        }

        @Test
        void returnsVersion12ForMoreThan0_12_0() {
            def plugin = new TerraformPlugin()

            def foundStrategy = plugin.strategyFor('0.12.3')

            assertThat(foundStrategy, instanceOf(TerraformPluginVersion12.class))
        }

    }

    class ReadFile {
        @Test
        void returnsTheContentsOfTheGivenFile() {
            def expectedFilename = 'someFilename'
            def expectedContent = 'someContent'
            def jenkinsOriginal = new Expando()
            jenkinsOriginal.readFile = { String filename ->
                assertEquals(expectedFilename, filename)
                return  expectedContent
            }
            def plugin = spy(new TerraformPlugin())
            doReturn(jenkinsOriginal).when(plugin).getJenkinsOriginal()

            def foundContent = plugin.readFile(expectedFilename)

            assertEquals(expectedContent, foundContent)
        }

        @Test
        void trimsWhitespaceFromTheFileContent() {
            def expectedFilename = 'someFilename'
            def expectedContent = 'someContent'
            def jenkinsOriginal = new Expando()
            jenkinsOriginal.readFile = { String filename ->
                assertEquals(expectedFilename, filename)
                return "  ${expectedContent}   "
            }
            def plugin = spy(new TerraformPlugin())
            doReturn(jenkinsOriginal).when(plugin).getJenkinsOriginal()

            def foundContent = plugin.readFile(expectedFilename)

            assertEquals(expectedContent, foundContent)
        }
    }

    class FileExists {
        @Test
        void returnsTrueIfFileExistsInWorkspace() {
            def expectedFilename = 'someFile'
            def jenkinsOriginal = new Expando()
            jenkinsOriginal.fileExists = { String filename ->
                assertEquals(expectedFilename, filename)
                return true
            }
            def plugin = spy(new TerraformPlugin())
            doReturn(jenkinsOriginal).when(plugin).getJenkinsOriginal()

            def isFound = plugin.fileExists(expectedFilename)

            assertTrue(isFound)
        }

        @Test
        void returnsFalseIfFileDoesNotExistInWorkspace() {
            def expectedFilename = 'someFile'
            def jenkinsOriginal = new Expando()
            jenkinsOriginal.fileExists = { String filename ->
                assertEquals(expectedFilename, filename)
                return false
            }
            def plugin = spy(new TerraformPlugin())
            doReturn(jenkinsOriginal).when(plugin).getJenkinsOriginal()

            def isFound = plugin.fileExists(expectedFilename)

            assertFalse(isFound)
        }
    }
}
