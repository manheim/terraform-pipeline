import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.startsWith
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class JenkinsfileTest {
    private Jenkinsfile jenkinsfile

    @Before
    public void setup() {
        jenkinsfile = new Jenkinsfile()
    }

    @Before
    @After
    void reset() {
        Jenkinsfile.reset()
    }

    class StandardizedRepoSlug {
        @Test
        void startsWithTheRepoOrganization() {
            def expectedOrg = "my_org"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/${expectedOrg}/myRepo.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getStandardizedRepoSlug()

            assertThat(result, startsWith(expectedOrg))
        }

        @Test
        void convertsOrgFromTitleCaseToSnakeCase() {
            def expectedOrg = "my_org"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/MyOrg/myRepo.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getStandardizedRepoSlug()

            assertThat(result, startsWith(expectedOrg))
        }

        @Test
        void endsWithTheRepoOrganization() {
            def expectedRepo = "my_repo"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/MyOrg/my_repo.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getStandardizedRepoSlug()

            assertThat(result, endsWith("/${expectedRepo}"))
        }

        @Test
        void convertsRepoFromTitleCaseToSnakeCase() {
            def expectedRepo = "my_repo"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/MyOrg/MyRepo.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getStandardizedRepoSlug()

            assertThat(result, endsWith("/${expectedRepo}"))
        }
    }

    public class ParseScmUrl {
        public class WithHttpUrl {
            public class WithHttp {
                @Test
                void returnsOrganization() {
                    String organization = "MyOrg"
                    Map results = jenkinsfile.parseScmUrl("http://my.github.com/${organization}/SomeRepo.git")

                    assertEquals(organization, results['organization'])
                }

                @Test
                void returnsRepo() {
                    String repo = "MyRepo"
                    Map results = jenkinsfile.parseScmUrl("http://my.github.com/SomeOrg/${repo}.git")

                    assertEquals(repo, results['repo'])
                }

                @Test
                void returnsBaseUrl() {
                    String expectedDomain = "my.github.com"
                    Map results = jenkinsfile.parseScmUrl("http://${expectedDomain}/SomeOrg/SomeRepo.git")

                    assertEquals(expectedDomain, results['domain'])
                }

                @Test
                void returnsTheProtocolUsed() {
                    String expectedProtocol = "http"
                    Map results = jenkinsfile.parseScmUrl("${expectedProtocol}://my.github.com/SomeOrg/SomeRepo.git")

                    assertEquals(expectedProtocol, results['protocol'])
                }
            }

            public class WithHttps {
                @Test
                void returnsOrganization() {
                    String organization = "MyOrg"
                    Map results = jenkinsfile.parseScmUrl("https://my.github.com/${organization}/SomeRepo.git")

                    assertEquals(organization, results['organization'])
                }

                @Test
                void returnsRepo() {
                    String repo = "MyRepo"
                    Map results = jenkinsfile.parseScmUrl("https://my.github.com/SomeOrg/${repo}.git")

                    assertEquals(repo, results['repo'])
                }

                @Test
                void returnsDomain() {
                    String expectedDomain = "my.github.com"
                    Map results = jenkinsfile.parseScmUrl("https://${expectedDomain}/SomeOrg/SomeRepo.git")

                    assertEquals(expectedDomain, results['domain'])
                }

                @Test
                void returnsTheProtocolUsed() {
                    String expectedProtocol = "https"
                    Map results = jenkinsfile.parseScmUrl("${expectedProtocol}://my.github.com/SomeOrg/SomeRepo.git")

                    assertEquals(expectedProtocol, results['protocol'])
                }
            }
        }

        public class WithSshUrl {
            @Test
            void returnsOrganization() {
                String organization = "MyOrg"
                Map results = jenkinsfile.parseScmUrl("git@my.github.com:${organization}/SomeRepo.git")

                assertEquals(organization, results['organization'])
            }

            @Test
            void returnsRepo() {
                String repo = "MyRepo"
                Map results = jenkinsfile.parseScmUrl("git@my.github.com:SomeOrg/${repo}.git")

                assertEquals(repo, results['repo'])
            }

            @Test
            void returnsDomain() {
                String expectedDomain = "my.github.com"
                Map results = jenkinsfile.parseScmUrl("git@${expectedDomain}/SomeOrg/SomeRepo.git")

                assertEquals(expectedDomain, results['domain'])
            }

            @Test
            void returnsTheProtocolUsed() {
                String expectedProtocol = 'git'
                Map results = jenkinsfile.parseScmUrl("${expectedProtocol}@my.github.com/SomeOrg/SomeRepo.git")

                assertEquals(expectedProtocol, results['protocol'])
            }
        }
    }

    public class GetRepoName {
        @Test
        void returnsTheUnmodifiedRepoName() {
            def expectedRepo = "MyRepoName"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/MyOrg/${expectedRepo}.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getRepoName()

            assertEquals(expectedRepo, result)
        }
    }

    public class GetOrganization {
        @Test
        void returnsTheUnmodifiedOrgName() {
            def expectedOrg = "MyOrgName"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/${expectedOrg}/MyRepo.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getOrganization()

            assertEquals(expectedOrg, result)
        }
    }

    public class Init {
        @Test
        void storesTheOriginalJenkinsfileReference() {
            def original = new DummyJenkinsfile()
            Jenkinsfile.init(original)

            assertEquals(original, Jenkinsfile.original)
        }
    }

    public class Build {
        private class DummyJenkinsfileWithTemplates extends DummyJenkinsfile {
            public Pipeline2Stage = { args -> }
        }

        class Scripted {
            @Test
            void usesDefaultTemplatesIfNonProvided() {
                def stage1 = mock(Stage.class)
                def stage2 = mock(Stage.class)
                def stages = [stage1, stage2]

                Jenkinsfile.declarative = null
                Jenkinsfile.build(stages)

                verify(stage1, times(1)).build()
                verify(stage2, times(1)).build()
            }
        }

        class Declarative {
            @Test
            void usesDefaultTemplatesIfNonProvided() {
                def original = new DummyJenkinsfileWithTemplates()
                original.Pipeline2Stage = spy(original.Pipeline2Stage)
                def stages = [mock(Stage.class), mock(Stage.class)]
                Jenkinsfile.declarative = true
                Jenkinsfile.original = original

                Jenkinsfile.build(stages)

                verify(original.Pipeline2Stage, times(1)).call(stages)
            }

            @Test
            void usesTheGivenPipelineTemplateToBuildTheStages() {
                def stages = [mock(Stage.class)]
                Closure newTemplate = spy { args -> }
                Jenkinsfile.pipelineTemplate = newTemplate
                Jenkinsfile.declarative = true

                Jenkinsfile.build(stages)

                verify(newTemplate, times(1)).call(stages)
            }
        }
    }

    class GetPipelineTemplate {
        private class DummyJenkinsfileWithTemplates extends DummyJenkinsfile {
            public Pipeline2Stage = { args -> }
            public Pipeline3Stage = { args -> }
            public Pipeline4Stage = { args -> }
            public Pipeline5Stage = { args -> }
            public Pipeline6Stage = { args -> }
            public Pipeline7Stage = { args -> }
        }

        private getNumberOfStages(int number) {
            def stages = []
            number.times {
                stages << mock(Stage.class)
            }

            return stages
        }

        @Test(expected = RuntimeException.class)
        void throwsAnErrorFor1Stage() {
            def stages = getNumberOfStages(1)

            Jenkinsfile.getPipelineTemplate(stages)
        }

        @Test
        void returnsThe2StageTemplateFor2Stages() {
            def stages = getNumberOfStages(2)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertEquals(original.Pipeline2Stage, actual)
        }

        @Test
        void returnsThe3StageTemplateFor3Stages() {
            def stages = getNumberOfStages(3)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertEquals(original.Pipeline3Stage, actual)
        }

        @Test
        void returnsThe4StageTemplateFor4Stages() {
            def stages = getNumberOfStages(4)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertEquals(original.Pipeline4Stage, actual)
        }

        @Test
        void returnsThe5StageTemplateFor5Stages() {
            def stages = getNumberOfStages(5)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertEquals(original.Pipeline5Stage, actual)
        }

        @Test
        void returnsThe6StageTemplateFor6Stages() {
            def stages = getNumberOfStages(6)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertEquals(original.Pipeline6Stage, actual)
        }

        @Test
        void returnsThe7StageTemplateFor7Stages() {
            def stages = getNumberOfStages(7)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertEquals(original.Pipeline7Stage, actual)
        }
    }

    public class GetNodeName {
        @After
        void reset() {
            Jenkinsfile.defaultNodeName = null
            Jenkinsfile.instance = null
        }

        private configureJenkins(Map config = [:]) {
            Jenkinsfile.instance = mock(Jenkinsfile.class)
            when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
            when(Jenkinsfile.instance.getRepoName()).thenReturn(config.repoName ?: 'repo')
            when(Jenkinsfile.instance.getOrganization()).thenReturn(config.organization ?: 'org')
            when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
        }

        @Test
        void returnsDefaultNodeNameWhenPresent() {
            String expectedName = "someName"
            Jenkinsfile.defaultNodeName = expectedName

            String actualName = Jenkinsfile.getNodeName()
            assertEquals(expectedName, actualName)
        }

        @Test
        void returnsDefaultNodeNameEvenWhenEnvironmentVariableGiven() {
            String expectedName = "expectedName"
            Jenkinsfile.defaultNodeName = expectedName
            configureJenkins(env: [ DEFAULT_NODE_NAME: 'wrongName' ])

            String actualName = Jenkinsfile.getNodeName()
            assertEquals(expectedName, actualName)
        }

        @Test
        void returnsEnvironmentVariableWhenDefaultNodeNameNotGiven() {
            String expectedName = 'expectedName'
            Jenkinsfile.defaultNodeName = null
            configureJenkins(env: [ DEFAULT_NODE_NAME: expectedName ])

            String actualName = Jenkinsfile.getNodeName()
            assertEquals(expectedName, actualName)
        }
    }

    class ReadFile {
        @Test
        void returnsNullIfTheFileDoesNotExist() {
            def filename = 'somefile'
            def original = spy(new DummyJenkinsfile())
            doReturn(false).when(original).fileExists(filename)
            Jenkinsfile.original = original

            def result = Jenkinsfile.readFile(filename)

            assertEquals(null, result)
        }

        @Test
        void returnsFileContentIfFileExists() {
            def expectedContent = 'someContent'
            def filename = 'somefile'
            def original = spy(new DummyJenkinsfile())
            doReturn(true).when(original).fileExists(filename)
            doReturn(expectedContent).when(original).readFile(filename)

            Jenkinsfile.original = original

            def result = Jenkinsfile.readFile(filename)

            assertEquals(expectedContent, result)
        }
    }

    private class MockRepo {
        String url
        public MockRepo(String url) {
            this.url = url
        }
    }

    private class MockScm {
        private List repos =  []

        public MockScm(String repoUrl) {
            repos << new MockRepo(repoUrl)
        }

        public List getUserRemoteConfigs() {
            return repos
        }
    }

    private mockScm(String url) {
        return new MockScm(url)
    }
}
