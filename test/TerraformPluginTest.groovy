import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformPluginTest {

    @Nested
    class VersionDetection {
        @Test
        void usesExplicitVersionIfProvided() {
            def expectedVersion = 'foo'
            def plugin = new TerraformPlugin()
            TerraformPlugin.withVersion(expectedVersion)

            def foundVersion = plugin.detectVersion()

            assertThat(expectedVersion, equalTo(foundVersion))
        }

        @Test
        void usesFileIfPresent() {
            def expectedVersion =  '0.12.0-foobar'
            MockJenkinsfile.withFile(TerraformPlugin.TERRAFORM_VERSION_FILE, expectedVersion)
            def plugin = new TerraformPlugin()

            def foundVersion = plugin.detectVersion()

            assertThat(expectedVersion, equalTo(foundVersion))
        }

        @Test
        void trimsWhitespaceFromFile() {
            def expectedVersion =  '0.12.0-foobar'
            MockJenkinsfile.withFile(TerraformPlugin.TERRAFORM_VERSION_FILE, "${expectedVersion}   ")
            def plugin = new TerraformPlugin()

            def foundVersion = plugin.detectVersion()

            assertThat(expectedVersion, equalTo(foundVersion))
        }

        @Test
        void usesDefaultIfFileNotFound() {
            def plugin = new TerraformPlugin()
            def original = spy(new MockWorkflowScript())
            doReturn(false).when(original).fileExists(TerraformPlugin.TERRAFORM_VERSION_FILE)
            Jenkinsfile.original = original

            def foundVersion = plugin.detectVersion()

            assertThat(TerraformPlugin.DEFAULT_VERSION, equalTo(foundVersion))
        }
    }

    @Nested
    class CheckVersion {
        // This can be fleshed out.  For now, jusst make sure it runs
        @Test
        void doesNotError() {
            Jenkinsfile.original = new MockWorkflowScript()
            TerraformPlugin.checkVersion()
        }
    }

    @Nested
    class WithVersion {
        @Test
        void usesVersionEvenIfFileExists() {
            TerraformPlugin.withVersion('2.0.0')
            assertThat('2.0.0', equalTo(TerraformPlugin.version))
        }
    }

    @Nested
    class Strategyfor {
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
        void returnsVersion15For0_15_0() {
            def plugin = new TerraformPlugin()

            def foundStrategy = plugin.strategyFor('0.15.0')

            assertThat(foundStrategy, instanceOf(TerraformPluginVersion15.class))
        }

        @Test
        void returnsVersion15ForMoreThan0_15_0() {
            def plugin = new TerraformPlugin()

            def foundStrategy = plugin.strategyFor('0.15.4')

            assertThat(foundStrategy, instanceOf(TerraformPluginVersion15.class))
        }

    }

    @Nested
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

    @Nested
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

    @Nested
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

    @Nested
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

    @Nested
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

    @Nested
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
