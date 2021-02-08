import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mockingDetails
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

    public static needsJenkinsfileInstanceMock() {
        return Jenkinsfile.instance == null || !mockingDetails(Jenkinsfile.instance).isMock()
    }

    public static mockJenkinsfileInstance() {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
    }
}
