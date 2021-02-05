import static org.mockito.Mockito.when
import static org.mockito.Mockito.mock

public class MockJenkinsfile {

    public static withEnv(Map env) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getEnv()).thenReturn(env)
    }
}
