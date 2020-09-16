import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformPluginTest {

    class VersionDetection {
        @Before
        @After
        void reset() {
            TerraformPlugin.reset()
            Jenkinsfile.reset()
        }

        @Test
        void usesExplicitVersionIfProvided() {
            def expectedVersion = 'foo'
            def plugin = new TerraformPlugin()
            TerraformPlugin.withVersion(expectedVersion)

            def foundVersion = plugin.detectVersion()

            assertEquals(expectedVersion, foundVersion)
        }

        @Test
        void usesFileIfPresent() {
            def expectedVersion =  '0.12.0-foobar'
            def plugin = new TerraformPlugin()
            def original = spy(new DummyJenkinsfile())
            doReturn(true).when(original).fileExists(TerraformPlugin.TERRAFORM_VERSION_FILE)
            doReturn(expectedVersion).when(original).readFile(TerraformPlugin.TERRAFORM_VERSION_FILE)
            Jenkinsfile.original = original

            def foundVersion = plugin.detectVersion()

            assertEquals(expectedVersion, foundVersion)
        }

        @Test
        void trimsWhitespaceFromFile() {
            def expectedVersion =  '0.12.0-foobar'
            def plugin = new TerraformPlugin()
            def original = spy(new DummyJenkinsfile())
            doReturn(true).when(original).fileExists(TerraformPlugin.TERRAFORM_VERSION_FILE)
            doReturn("${expectedVersion}   ").when(original).readFile(TerraformPlugin.TERRAFORM_VERSION_FILE)
            Jenkinsfile.original = original

            def foundVersion = plugin.detectVersion()

            assertEquals(expectedVersion, foundVersion)
        }

        @Test
        void usesDefaultIfFileNotFound() {
            def plugin = new TerraformPlugin()
            def original = spy(new DummyJenkinsfile())
            doReturn(false).when(original).fileExists(TerraformPlugin.TERRAFORM_VERSION_FILE)
            Jenkinsfile.original = original

            def foundVersion = plugin.detectVersion()

            assertEquals(TerraformPlugin.DEFAULT_VERSION, foundVersion)
        }
    }

    class CheckVersion {
        @Before
        @After
        void reset() {
            TerraformPlugin.reset()
            Jenkinsfile.reset()
        }

        // This can be fleshed out.  For now, jusst make sure it runs
        @Test
        void doesNotError() {
            Jenkinsfile.original = new DummyJenkinsfile()
            TerraformPlugin.checkVersion()
        }
    }

    class WithVersion {
        @After
        void reset() {
            TerraformPlugin.reset()
        }

        @Test
        void usesVersionEvenIfFileExists() {
            TerraformPlugin.withVersion('2.0.0')
            assertEquals('2.0.0', TerraformPlugin.version)
        }
    }

    class Strategyfor {
        @After
        void reset() {
            TerraformPlugin.reset()
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

    class ApplyTerraformFormatCommand {
        @Test
        void shouldApplyTheCorrectStrategyToTerraformFormatCommand() {
            def formatCommand = mock(TerraformFormatCommand.class)
            def strategy = mock(TerraformPluginVersion.class)
            def plugin = spy(new TerraformPlugin())
            doReturn('someVersion').when(plugin).detectVersion()
            doReturn(strategy).when(plugin).strategyFor('someVersion')

            plugin.apply(formatCommand)

            verify(strategy, times(1)).apply(formatCommand)
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
