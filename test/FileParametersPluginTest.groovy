import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class FileParametersPluginTest {
    public class Init {
        @After
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

    public class GetVariables {
        @Before
        void setupJenkinsfile() {
            Jenkinsfile.original = new DummyJenkinsfile()
        }

        @After
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

