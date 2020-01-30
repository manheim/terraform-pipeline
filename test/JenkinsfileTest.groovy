import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import Jenkinsfile

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.hamcrest.Matchers.*


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

                    assertEquals(results['organization'], organization)
                }

                @Test
                void returnsRepo() {
                    String repo = "MyRepo"
                    Map results = jenkinsfile.parseScmUrl("http://my.github.com/SomeOrg/${repo}.git")

                    assertEquals(results['repo'], repo)
                }
            }

            public class WithHttps {
                @Test
                void returnsOrganization() {
                    String organization = "MyOrg"
                    Map results = jenkinsfile.parseScmUrl("https://my.github.com/${organization}/SomeRepo.git")

                    assertEquals(results['organization'], organization)
                }

                @Test
                void returnsRepo() {
                    String repo = "MyRepo"
                    Map results = jenkinsfile.parseScmUrl("https://my.github.com/SomeOrg/${repo}.git")

                    assertEquals(results['repo'], repo)
                }
            }
        }

        public class WithSshUrl {
            @Test
            void returnsOrganization() {
                String organization = "MyOrg"
                Map results = jenkinsfile.parseScmUrl("git@my.github.com:${organization}/SomeRepo.git")

                assertEquals(results['organization'], organization)
            }

            @Test
            void returnsRepo() {
                String repo = "MyRepo"
                Map results = jenkinsfile.parseScmUrl("git@my.github.com:SomeOrg/${repo}.git")

                assertEquals(results['repo'], repo)
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
            assertEquals(actualName, expectedName)
        }

        @Test
        void returnsDefaultNodeNameEvenWhenEnvironmentVariableGiven() {
            String expectedName = "expectedName"
            Jenkinsfile.defaultNodeName = expectedName
            configureJenkins(env: [ DEFAULT_NODE_NAME: 'wrongName' ])

            String actualName = Jenkinsfile.getNodeName()
            assertEquals(actualName, expectedName)
        }

        @Test
        void returnsEnvironmentVariableWhenDefaultNodeNameNotGiven() {
            String expectedName = 'expectedName'
            Jenkinsfile.defaultNodeName = null
            configureJenkins(env: [ DEFAULT_NODE_NAME: expectedName ])

            String actualName = Jenkinsfile.getNodeName()
            assertEquals(actualName, expectedName)
        }
    }
}
