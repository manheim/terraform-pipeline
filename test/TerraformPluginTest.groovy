import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

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

    class ApplyTerraformValidateCommand {
        @Test
        void shouldApplyTheCorrectStrategyToTerraformValidateCommand() {
            def validateCommand = mock(TerraformValidateCommand.class)
            def strategy = mock(TerraformPluginVersion.class)
            def plugin = spy(new TerraformPlugin())
            doReturn('someVersion').when(plugin).detectVersion()
            doReturn(strategy).when(plugin).strategyFor('someVersion')

            plugin.apply(validateCommand)

            verify(strategy, times(1)).apply(validateCommand)
        }
    }

    class ApplyTerraformPlanCommand {
        @Test
        void shouldApplyTheCorrectStrategyToTerraformPlanCommand() {
            def planCommand = mock(TerraformPlanCommand.class)
            def strategy = mock(TerraformPluginVersion.class)
            def plugin = spy(new TerraformPlugin())
            doReturn('someVersion').when(plugin).detectVersion()
            doReturn(strategy).when(plugin).strategyFor('someVersion')

            plugin.apply(planCommand)

            verify(strategy, times(1)).apply(planCommand)
        }
    }

    class ApplyTerraformApplyCommand {
        @Test
        void shouldApplyTheCorrectStrategyToTerraformApplyCommand() {
            def applyCommand = mock(TerraformApplyCommand.class)
            def strategy = mock(TerraformPluginVersion.class)
            def plugin = spy(new TerraformPlugin())
            doReturn('someVersion').when(plugin).detectVersion()
            doReturn(strategy).when(plugin).strategyFor('someVersion')

            plugin.apply(applyCommand)

            verify(strategy, times(1)).apply(applyCommand)
        }
    }

    class ApplyTerraformValidateStage {
        @Test
        void shouldDecorateTheGivenStage() {
            def validateStage = mock(TerraformValidateStage.class)
            def expectedDecoration = mock(Closure.class)
            def plugin = spy(new TerraformPlugin())
            doReturn(expectedDecoration).when(plugin).modifyValidateStage(validateStage)

            plugin.apply(validateStage)

            verify(validateStage, times(1)).decorate(TerraformValidateStage.ALL, expectedDecoration)
        }
    }

    class ModifyTerraformValidateStage {
        @Test
        void shouldApplyTheCorrectStrategyToTerraformValidateStage() {
            def expectedVersion = 'someVersion'
            def validateStage = mock(TerraformValidateStage.class)
            def strategy = mock(TerraformPluginVersion.class)
            def plugin = spy(new TerraformPlugin())
            doReturn(expectedVersion).when(plugin).detectVersion()
            doReturn(strategy).when(plugin).strategyFor(expectedVersion)

            def closure = plugin.modifyValidateStage(validateStage)
            closure.call { -> }

            verify(strategy, times(1)).apply(validateStage)
        }

        @Test
        void shouldCallTheSubsequentClosureWhenDone() {
            def version = 'someVersion'
            def validateStage = mock(TerraformValidateStage.class)
            def strategy = mock(TerraformPluginVersion.class)
            def plugin = spy(new TerraformPlugin())
            doReturn(version).when(plugin).detectVersion()
            doReturn(strategy).when(plugin).strategyFor(version)
            def subsequentClosure = mock(Closure.class)

            def closure = plugin.modifyValidateStage(validateStage)
            closure.call(subsequentClosure)

            verify(subsequentClosure, times(1)).call()
        }
    }
}
