import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FileParametersPluginTest {
    @Nested
    public class Init {
        @AfterEach
        void resetPlugins() {
            TerraformEnvironmentStage.reset()
        }

        @Test
        void modifiesTerraformEnvironmentStage() {
            FileParametersPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(FileParametersPlugin.class)))
        }
    }

    @Nested
    public class GetVariables {
        @BeforeEach
        void setupJenkinsfile() {
            Jenkinsfile.original = new DummyJenkinsfile()
        }

        @AfterEach
        void reset() {
            Jenkinsfile.reset()
        }

        @Test
        void returnsAValueForEachLine() {
            List expectedValues = [ "VAR1=VALUE1", "VAR2=VALUE2" ]
            String fileContents = expectedValues.join('\n')

            FileParametersPlugin plugin = new FileParametersPlugin()
            List actualValues = plugin.getVariables(fileContents)

            assertEquals(expectedValues, actualValues)
        }

        @Test
        void ignoresTrailingNewline() {
            List expectedValues = [ "VAR1=VALUE1", "VAR2=VALUE2" ]
            String fileContents = expectedValues.join('\n') + '\n\n'

            FileParametersPlugin plugin = new FileParametersPlugin()
            List actualValues = plugin.getVariables(fileContents)

            assertEquals(expectedValues, actualValues)
        }

        @Test
        void handlesCarriageReturnCharacters() {
            List expectedValues = [ "VAR1=VALUE1", "VAR2=VALUE2" ]
            String fileContents = expectedValues.join('\r\n')

            FileParametersPlugin plugin = new FileParametersPlugin()
            List actualValues = plugin.getVariables(fileContents)

            assertEquals(expectedValues, actualValues)
        }

        @Test
        void interpolatesReferencesToOtherEnvironmentVariables() {
            String fileContents = 'SOME_VARIABLE=${env.OTHER_VARIABLE}'

            FileParametersPlugin plugin = spy(new FileParametersPlugin())
            doReturn([ OTHER_VARIABLE: 'VALUE1']).when(plugin).getEnv()

            List actualValues = plugin.getVariables(fileContents)

            assertEquals(["SOME_VARIABLE=VALUE1"], actualValues)
        }
    }
}

