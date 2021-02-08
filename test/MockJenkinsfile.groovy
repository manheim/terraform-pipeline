import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mockingDetails
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.when

public class MockJenkinsfile {

    public static withEnv(Map env = [:]) {
        if (needsJenkinsfileInstanceMock()) {
            mockJenkinsfileInstance()
        }

        when(Jenkinsfile.instance.getEnv()).thenReturn(env)
        return this
    }

    public static withMockedOriginal() {
        Jenkinsfile.original = new DummyJenkinsfile()
        return this
    }

    public static withStandardizedRepoSlug(String slug) {
        if (needsJenkinsfileInstanceMock()) {
            mockJenkinsfileInstance()
        }

        when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(slug)
        return this
    }

    public static withRepoName(String repoName) {
        if (needsJenkinsfileInstanceMock()) {
            mockJenkinsfileInstance()
        }

        when(Jenkinsfile.instance.getRepoName()).thenReturn(repoName)
        return this
    }

    public static withOrganization(String organization) {
        if (needsJenkinsfileInstanceMock()) {
            mockJenkinsfileInstance()
        }

        when(Jenkinsfile.instance.getOrganization()).thenReturn(organization)
        return this
    }

    public static withFile(String filePath, String fileContent = '') {
        def original = spy(new DummyJenkinsfile())
        doReturn(true).when(original).fileExists(filePath)
        doReturn(fileContent).when(original).readFile(filePath)

        Jenkinsfile.original = original
        return this
    }

    public static needsJenkinsfileInstanceMock() {
        return Jenkinsfile.instance == null || !mockingDetails(Jenkinsfile.instance).isMock()
    }

    public static mockJenkinsfileInstance() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
    }
}
