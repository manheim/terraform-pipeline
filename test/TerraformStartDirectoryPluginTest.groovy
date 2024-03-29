import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformStartDirectoryPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            TerraformStartDirectoryPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformStartDirectoryPlugin.class)))
        }

        @Test
        void modifiesTerraformValidateStage() {
            TerraformStartDirectoryPlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformStartDirectoryPlugin.class)))
        }
    }

    @Nested
    class Apply {
        @Test
        void addsDirectoryClosureToTerraformEnvironmentStage() {
            def expectedClosure = { -> }
            def plugin = spy(new TerraformStartDirectoryPlugin())
            doReturn(expectedClosure).when(plugin).addDirectory()
            def stage = mock(TerraformEnvironmentStage.class)

            plugin.apply(stage)

            verify(stage).decorate(anyString(), eq(expectedClosure))
        }

        @Test
        void addsDirectoryClosureToTerraformValidateStage() {
            def expectedClosure = { -> }
            def plugin = spy(new TerraformStartDirectoryPlugin())
            doReturn(expectedClosure).when(plugin).addDirectory()
            def stage = mock(TerraformValidateStage.class)

            plugin.apply(stage)

            verify(stage).decorate(anyString(), eq(expectedClosure))
        }
    }

    @Nested
    class WithDirectory {
        @Test
        void isFluent() {
            def result = TerraformStartDirectoryPlugin.withDirectory('./customDirectory/')

            assertThat(result, equalTo(TerraformStartDirectoryPlugin.class))
        }
    }

    @Nested
    class AddDirectory {
        @Nested
        class WithDirectory {
            private String expectedDirectory = './customDirectory/'
            @BeforeEach
            void addDirectory() {
                TerraformStartDirectoryPlugin.withDirectory(expectedDirectory)
            }

            @Test
            void runsTheNestedClosure() {
                def plugin = new TerraformStartDirectoryPlugin()
                def iWasCalled = false
                def nestedClosure = { -> iWasCalled = true }

                def addDirectoryClosure = plugin.addDirectory()
                addDirectoryClosure.delegate = new MockWorkflowScript()
                addDirectoryClosure(nestedClosure)

                assertThat(iWasCalled, equalTo(true))
            }

            @Test
            void useDirectory() {
                def mockWorkflowScript = spy(new MockWorkflowScript())
                def plugin = new TerraformStartDirectoryPlugin()

                def addDirectoryClosure = plugin.addDirectory()
                addDirectoryClosure.delegate = mockWorkflowScript
                addDirectoryClosure() { -> }

                verify(mockWorkflowScript).dir(eq(expectedDirectory), any(Closure.class))
            }
        }
    }
}
