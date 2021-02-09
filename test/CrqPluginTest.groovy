import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class CrqPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            CrqPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(CrqPlugin.class)))
        }
    }

    @Nested
    public class AddCrq {
        @Nested
        public class withCrqEnvironment {
            @Test
            public void shouldExecutePipeline() {
                def plugin = new CrqPlugin()
                MockJenkinsfile.withEnv('CRQ_ENVIRONMENT': 'MyCrqEnv')
                //when(plugin.getrepoSlug: 'Org/Repo'))
                Closure crqClosure = plugin.addCrq('myEnv')
                Closure pipeline = mock(Closure.class)

                crqClosure.delegate = pipeline
                crqClosure.resolveStrategy = Closure.DELEGATE_FIRST
                crqClosure.call(pipeline)

                verify(pipeline).call()
            }

            // @Test shouldCallRemedierOpen
        }

        @Nested
        public class withoutCrqEnvironment {
            @Test
            public void shouldExecutePipeline() {
                MockJenkinsfile.withEnv()
                def plugin = new CrqPlugin()
                Closure crqClosure = plugin.addCrq('myEnv')
                Closure pipeline = mock(Closure.class)

                crqClosure.delegate = pipeline
                crqClosure.resolveStrategy = Closure.DELEGATE_FIRST
                crqClosure.call(pipeline)

                verify(pipeline).call()
            }

            // @Test shouldNotCallRemedierOpen
        }
    }

    @Nested
    public class GetCrqEnviroment {
        @Test
        void returnsCrqEnvirommentIfPresent() {
            String expectedCrqEnvironment = 'someEnvironment'
            MockJenkinsfile.withEnv('CRQ_ENVIRONMENT': expectedCrqEnvironment)
            CrqPlugin plugin = new CrqPlugin()

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(expectedCrqEnvironment))
        }

        @Test
        void returnsEnvironmentSpecificCrqEnvirommentIfPresent() {
            String expectedCrqEnvironment = 'someEnvironment'
            MockJenkinsfile.withEnv('MYENV_CRQ_ENVIRONMENT': expectedCrqEnvironment)
            CrqPlugin plugin = new CrqPlugin()

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(expectedCrqEnvironment))
        }

        @Test
        void prefersNonPrefixedCrqOverPrefixedCrq() {
            // The UseCase:
            //     Set PROD_CRQ_ENVIRONMENT as a global variable, to make multiple pipeline's prod behave the same way
            //     Set CRQ_ENVIRONMENT in a specific app's prod environment (eg: parameterstore), to override the global value
            String nonPrefixedCrq = 'nonPrefixed'
            String prefixedCrq = 'prefixed'
            MockJenkinsfile.withEnv(
                'CRQ_ENVIRONMENT': nonPrefixedCrq,
                'MYENV_CRQ_ENVIRONMENT': prefixedCrq
            )
            CrqPlugin plugin = new CrqPlugin()

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(nonPrefixedCrq))
        }

    }
}

