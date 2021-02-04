import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JenkinsfileTest {
    private Jenkinsfile jenkinsfile

    @BeforeEach
    public void setup() {
        jenkinsfile = new Jenkinsfile()
    }

    @BeforeEach
    @AfterEach
    void reset() {
        Jenkinsfile.reset()
    }

    @Nested
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

    @Nested
    public class ParseScmUrl {
        @Nested
        public class WithHttpUrl {
            @Nested
            public class WithHttp {
                @Test
                void returnsOrganization() {
                    String organization = "MyOrg"
                    Map results = jenkinsfile.parseScmUrl("http://my.github.com/${organization}/SomeRepo.git")

                    assertThat(results['organization'], equalTo(organization))
                }

                @Test
                void returnsRepo() {
                    String repo = "MyRepo"
                    Map results = jenkinsfile.parseScmUrl("http://my.github.com/SomeOrg/${repo}.git")

                    assertThat(results['repo'], equalTo(repo))
                }

                @Test
                void returnsBaseUrl() {
                    String expectedDomain = "my.github.com"
                    Map results = jenkinsfile.parseScmUrl("http://${expectedDomain}/SomeOrg/SomeRepo.git")

                    assertThat(results['domain'], equalTo(expectedDomain))
                }

                @Test
                void returnsTheProtocolUsed() {
                    String expectedProtocol = "http"
                    Map results = jenkinsfile.parseScmUrl("${expectedProtocol}://my.github.com/SomeOrg/SomeRepo.git")

                    assertThat(results['protocol'], equalTo(expectedProtocol))
                }
            }

            @Nested
            public class WithHttps {
                @Test
                void returnsOrganization() {
                    String organization = "MyOrg"
                    Map results = jenkinsfile.parseScmUrl("https://my.github.com/${organization}/SomeRepo.git")

                    assertThat(results['organization'], equalTo(organization))
                }

                @Test
                void returnsRepo() {
                    String repo = "MyRepo"
                    Map results = jenkinsfile.parseScmUrl("https://my.github.com/SomeOrg/${repo}.git")

                    assertThat(results['repo'], equalTo(repo))
                }

                @Test
                void returnsDomain() {
                    String expectedDomain = "my.github.com"
                    Map results = jenkinsfile.parseScmUrl("https://${expectedDomain}/SomeOrg/SomeRepo.git")

                    assertThat(results['domain'], equalTo(expectedDomain))
                }

                @Test
                void returnsTheProtocolUsed() {
                    String expectedProtocol = "https"
                    Map results = jenkinsfile.parseScmUrl("${expectedProtocol}://my.github.com/SomeOrg/SomeRepo.git")

                    assertThat(results['protocol'], equalTo(expectedProtocol))
                }
            }
        }

        @Nested
        public class WithSshUrl {
            @Test
            void returnsOrganization() {
                String organization = "MyOrg"
                Map results = jenkinsfile.parseScmUrl("git@my.github.com:${organization}/SomeRepo.git")

                assertThat(results['organization'], equalTo(organization))
            }

            @Test
            void returnsRepo() {
                String repo = "MyRepo"
                Map results = jenkinsfile.parseScmUrl("git@my.github.com:SomeOrg/${repo}.git")

                assertThat(results['repo'], equalTo(repo))
            }

            @Test
            void returnsDomain() {
                String expectedDomain = "my.github.com"
                Map results = jenkinsfile.parseScmUrl("git@${expectedDomain}/SomeOrg/SomeRepo.git")

                assertThat(results['domain'], equalTo(expectedDomain))
            }

            @Test
            void returnsTheProtocolUsed() {
                String expectedProtocol = 'git'
                Map results = jenkinsfile.parseScmUrl("${expectedProtocol}@my.github.com/SomeOrg/SomeRepo.git")

                assertThat(results['protocol'], equalTo(expectedProtocol))
            }
        }
    }

    @Nested
    public class GetRepoName {
        @Test
        void returnsTheUnmodifiedRepoName() {
            def expectedRepo = "MyRepoName"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/MyOrg/${expectedRepo}.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getRepoName()

            assertThat(result, equalTo(expectedRepo))
        }
    }

    @Nested
    public class GetOrganization {
        @Test
        void returnsTheUnmodifiedOrgName() {
            def expectedOrg = "MyOrgName"
            def original = spy(new DummyJenkinsfile())
            original.scm = mockScm("https://github.com/${expectedOrg}/MyRepo.git")
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()
            def result = instance.getOrganization()

            assertThat(result, equalTo(expectedOrg))
        }
    }

    @Nested
    public class Init {
        @Test
        void storesTheOriginalJenkinsfileReference() {
            def original = new DummyJenkinsfile()
            Jenkinsfile.init(original)

            assertThat(Jenkinsfile.original, equalTo(original))
        }
    }

    @Nested
    public class Build {
        private class DummyJenkinsfileWithTemplates extends DummyJenkinsfile {
            public Pipeline2Stage = { args -> }
        }

        @Nested
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

        @Nested
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

    @Nested
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

        @Test
        void throwsAnErrorFor1Stage() {
            def stages = getNumberOfStages(1)

            assertThrows(RuntimeException.class) {
                Jenkinsfile.getPipelineTemplate(stages)
            }
        }

        @Test
        void returnsThe2StageTemplateFor2Stages() {
            def stages = getNumberOfStages(2)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertThat(actual, equalTo(original.Pipeline2Stage))
        }

        @Test
        void returnsThe3StageTemplateFor3Stages() {
            def stages = getNumberOfStages(3)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertThat(actual, equalTo(original.Pipeline3Stage))
        }

        @Test
        void returnsThe4StageTemplateFor4Stages() {
            def stages = getNumberOfStages(4)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertThat(actual, equalTo(original.Pipeline4Stage))
        }

        @Test
        void returnsThe5StageTemplateFor5Stages() {
            def stages = getNumberOfStages(5)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertThat(actual, equalTo(original.Pipeline5Stage))
        }

        @Test
        void returnsThe6StageTemplateFor6Stages() {
            def stages = getNumberOfStages(6)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertThat(actual, equalTo(original.Pipeline6Stage))
        }

        @Test
        void returnsThe7StageTemplateFor7Stages() {
            def stages = getNumberOfStages(7)
            def original = spy(new DummyJenkinsfileWithTemplates())
            Jenkinsfile.original = original

            def actual = Jenkinsfile.getPipelineTemplate(stages)

            assertThat(actual, equalTo(original.Pipeline7Stage))
        }
    }

    @Nested
    class GetEnv {
        @Test
        void returnsThe7StageTemplateFor7Stages() {
            def expected = [key: 'value']
            def original = spy(new DummyJenkinsfile())
            original.env = expected
            Jenkinsfile.original = original
            def instance = new Jenkinsfile()

            def actual = instance.getEnv()

            assertThat(actual, equalTo(expected))
        }

    }

    @Nested
    public class GetNodeName {
        @AfterEach
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
            assertThat(actualName, equalTo(expectedName))
        }

        @Test
        void returnsDefaultNodeNameEvenWhenEnvironmentVariableGiven() {
            String expectedName = "expectedName"
            Jenkinsfile.defaultNodeName = expectedName
            configureJenkins(env: [ DEFAULT_NODE_NAME: 'wrongName' ])

            String actualName = Jenkinsfile.getNodeName()
            assertThat(actualName, equalTo(expectedName))
        }

        @Test
        void returnsEnvironmentVariableWhenDefaultNodeNameNotGiven() {
            String expectedName = 'expectedName'
            Jenkinsfile.defaultNodeName = null
            configureJenkins(env: [ DEFAULT_NODE_NAME: expectedName ])

            String actualName = Jenkinsfile.getNodeName()
            assertThat(actualName, equalTo(expectedName))
        }
    }

    @Nested
    class ReadFile {
        @Test
        void returnsNullIfTheFileDoesNotExist() {
            def filename = 'somefile'
            def original = spy(new DummyJenkinsfile())
            doReturn(false).when(original).fileExists(filename)
            Jenkinsfile.original = original

            def result = Jenkinsfile.readFile(filename)

            assertThat(result, equalTo(null))
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

            assertThat(result, equalTo(expectedContent))
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
