import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
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
class AgentNodePluginTest {
    private createOriginalSpy() {
        def workflowScript = spy(new MockWorkflowScript())
        workflowScript.docker = workflowScript

        return workflowScript
    }

    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStage() {
            AgentNodePlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(AgentNodePlugin.class)))
        }

        @Test
        void modifiesTerraformValidateStage() {
            AgentNodePlugin.init()

            Collection actualPlugins = TerraformValidateStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(AgentNodePlugin.class)))
        }
    }

    @Nested
    class Apply {
        @Test
        void addsAgentClosureToTerraformEnvironmentStage() {
            def expectedClosure = { -> }
            def plugin = spy(new AgentNodePlugin())
            doReturn(expectedClosure).when(plugin).addAgent()
            def stage = mock(TerraformEnvironmentStage.class)

            plugin.apply(stage)

            verify(stage).decorate(anyString(), eq(expectedClosure))
        }

        @Test
        void addsAgentClosureToTerraformValidateStage() {
            def expectedClosure = { -> }
            def plugin = spy(new AgentNodePlugin())
            doReturn(expectedClosure).when(plugin).addAgent()
            def stage = mock(TerraformValidateStage.class)

            plugin.apply(stage)

            verify(stage).decorate(anyString(), eq(expectedClosure))
        }
    }

    @Nested
    class AddAgent {
        @Nested
        class WithNoDockerEnabled {
            @Test
            void callsTheInnerclosure() {
                def plugin = new AgentNodePlugin()
                def innerClosure = spy { -> }

                def agentClosure = plugin.addAgent()

                agentClosure(innerClosure)

                verify(innerClosure).call()
            }
        }

        @Nested
        class WithDockerImageNoDockerfile {
            private String expectedImage = 'someImage'
            @BeforeEach
            void useDockerImage() {
                AgentNodePlugin.withAgentDockerImage(expectedImage)
            }

            @Test
            void callsTheInnerClosure() {
                def plugin = new AgentNodePlugin()
                def innerClosure = spy { -> }

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = new MockWorkflowScript()
                agentClosure(innerClosure)

                verify(innerClosure).call()
            }

            @Test
            void usesTheGivenDockerImage() {
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).image(expectedImage)
            }

            @Test
            void usesTheGivenDockerOptions() {
                def expectedOptions = 'someOptions'
                AgentNodePlugin.withAgentDockerImageOptions(expectedOptions)
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()
                original.docker = original

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).inside(eq(expectedOptions), anyObject())
            }
        }

        @Nested
        class WithDockerImageAndDockerfile {
            private String expectedImage = 'someImage'
            @BeforeEach
            void useDockerImage() {
                AgentNodePlugin.withAgentDockerImage(expectedImage)
            }

            @Test
            void callsTheInnerClosure() {
                AgentNodePlugin.withAgentDockerfile()
                def plugin = new AgentNodePlugin()
                def innerClosure = spy { -> }

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = new MockWorkflowScript()
                agentClosure(innerClosure)

                verify(innerClosure).call()
            }

            @Test
            void usesTheGivenDockerImage() {
                AgentNodePlugin.withAgentDockerfile()
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).build(eq(expectedImage), anyString())
            }

            @Test
            void worksWithoutBuildOptions() {
                def expectedBuildCommand = '-f Dockerfile .'
                AgentNodePlugin.withAgentDockerfile('Dockerfile')
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).build(anyString(), eq(expectedBuildCommand))
            }

            @Test
            void usesTheGivenDockerBuildOptions() {
                def expectedOptions = 'expectedOptions'
                AgentNodePlugin.withAgentDockerfile()
                               .withAgentDockerBuildOptions(expectedOptions)
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).build(anyString(), contains(expectedOptions))
            }

            @Test
            void usesFileNamedDockerfileByDefault() {
                AgentNodePlugin.withAgentDockerfile()
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).build(anyString(), contains('-f Dockerfile'))
            }

            @Test
            void usesGivenDockerfile() {
                def expectedDockerfile = 'someDockerfile'
                AgentNodePlugin.withAgentDockerfile(expectedDockerfile)
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).build(anyString(), contains("-f ${expectedDockerfile}"))
            }

            @Test
            void usesGivenDockerOptions() {
                def expectedOptions = 'someOptions'
                AgentNodePlugin.withAgentDockerfile()
                               .withAgentDockerImageOptions(expectedOptions)
                def plugin = new AgentNodePlugin()
                def original = createOriginalSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = original
                agentClosure { -> }

                verify(original).inside(eq(expectedOptions), anyObject())
            }
        }
    }
}

