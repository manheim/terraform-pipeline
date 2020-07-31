import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
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
}
