import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.*

@RunWith(HierarchicalContextRunner.class)
class FileParametersPluginTest {
    public class Init {
        @After
        void resetPlugins() {
            TerraformEnvironmentStage.resetPlugins()
        }

        @Test
        void modifiesTerraformEnvironmentStage() {
            FileParametersPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(FileParametersPlugin.class)))
        }
    }

    public class GetVariables {
        @After
        public void resetJenkinsfile() {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
        }

        private configureJenkinsfile(Map config = [:]) {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
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
            configureJenkinsfile(env: [ OTHER_VARIABLE: 'VALUE1' ])
            String fileContents = 'SOME_VARIABLE=${env.OTHER_VARIABLE}'

            FileParametersPlugin plugin = new FileParametersPlugin()
            List actualValues = plugin.getVariables(fileContents)

            assertEquals(["SOME_VARIABLE=VALUE1"], actualValues)
        }
    }
}

