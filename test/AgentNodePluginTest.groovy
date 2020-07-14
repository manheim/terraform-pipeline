import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Test
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class AgentNodePluginTest {
    private createJenkinsfileSpy() {
        def dummyJenkinsfile = spy(new DummyJenkinsfile())
        dummyJenkinsfile.docker = dummyJenkinsfile

        return dummyJenkinsfile
    }

    public class Init {
        @After
        void resetPlugins() {
            TerraformValidateStage.resetPlugins()
            TerraformEnvironmentStage.resetPlugins()
        }

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

    class AddAgent {
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

        class WithDockerImageNoDockerfile {
            private String expectedImage = 'someImage'
            @Before
            void useDockerImage() {
                AgentNodePlugin.withAgentDockerImage(expectedImage)
            }

            @After
            void reset() {
                AgentNodePlugin.reset()
            }

            @Test
            void callsTheInnerClosure() {
                def plugin = new AgentNodePlugin()
                def innerClosure = spy { -> }

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = new DummyJenkinsfile()
                agentClosure(innerClosure)

                verify(innerClosure).call()
            }

            @Test
            void usesTheGivenDockerImage() {
                def plugin = new AgentNodePlugin()
                def jenkinsfile = createJenkinsfileSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = jenkinsfile
                agentClosure { -> }

                verify(jenkinsfile).image(expectedImage)
            }

            @Test
            void usesTheGivenDockerOptions() {
                def expectedOptions = 'someOptions'
                AgentNodePlugin.withAgentDockerImageOptions(expectedOptions)
                def plugin = new AgentNodePlugin()
                def jenkinsfile = createJenkinsfileSpy()
                jenkinsfile.docker = jenkinsfile

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = jenkinsfile
                agentClosure { -> }

                verify(jenkinsfile).inside(eq(expectedOptions), anyObject())
            }
        }

        class WithDockerImageAndDockerfile {
            private String expectedImage = 'someImage'
            @Before
            void useDockerImage() {
                AgentNodePlugin.withAgentDockerImage(expectedImage)
            }

            @After
            void reset() {
                AgentNodePlugin.reset()
            }

            @Test
            void callsTheInnerClosure() {
                AgentNodePlugin.withAgentDockerfile()
                def plugin = new AgentNodePlugin()
                def innerClosure = spy { -> }

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = new DummyJenkinsfile()
                agentClosure(innerClosure)

                verify(innerClosure).call()
            }

            @Test
            void usesTheGivenDockerImage() {
                AgentNodePlugin.withAgentDockerfile()
                def plugin = new AgentNodePlugin()
                def jenkinsfile = createJenkinsfileSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = jenkinsfile
                agentClosure { -> }

                verify(jenkinsfile).build(eq(expectedImage), anyString())
            }

            @Test
            void usesTheGivenDockerBuildOptions() {
                def expectedOptions = 'expectedOptions'
                AgentNodePlugin.withAgentDockerfile()
                               .withAgentDockerBuildOptions(expectedOptions)
                def plugin = new AgentNodePlugin()
                def jenkinsfile = createJenkinsfileSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = jenkinsfile
                agentClosure { -> }

                verify(jenkinsfile).build(anyString(), contains(expectedOptions))
            }

            @Test
            void usesFileNamedDockerfileByDefault() {
                AgentNodePlugin.withAgentDockerfile()
                def plugin = new AgentNodePlugin()
                def jenkinsfile = createJenkinsfileSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = jenkinsfile
                agentClosure { -> }

                verify(jenkinsfile).build(anyString(), contains('-f Dockerfile'))
            }

            @Test
            void usesGivenDockerfile() {
                def expectedDockerfile = 'someDockerfile'
                AgentNodePlugin.withAgentDockerfile(expectedDockerfile)
                def plugin = new AgentNodePlugin()
                def jenkinsfile = createJenkinsfileSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = jenkinsfile
                agentClosure { -> }

                verify(jenkinsfile).build(anyString(), contains("-f ${expectedDockerfile}"))
            }

            @Test
            void usesGivenDockerOptions() {
                def expectedOptions = 'someOptions'
                AgentNodePlugin.withAgentDockerfile()
                               .withAgentDockerImageOptions(expectedOptions)
                def plugin = new AgentNodePlugin()
                def jenkinsfile = createJenkinsfileSpy()

                def agentClosure = plugin.addAgent()
                agentClosure.delegate = jenkinsfile
                agentClosure { -> }

                verify(jenkinsfile).inside(eq(expectedOptions), anyObject())
            }
        }
    }
}

