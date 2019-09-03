import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*;

@RunWith(HierarchicalContextRunner.class)
class CrqPluginTest {
    @After
    void resetJenkins() {
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

    public class Init {
        @After
        void resetPlugins() {
            TerraformEnvironmentStage.resetPlugins()
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            CrqPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(CrqPlugin.class)))
        }
    }

    public class AddCrq {
        public class withCrqEnvironment {
            @Test
            public void shouldExecutePipeline() {
                configureJenkins(env: ['CRQ_ENVIRONMENT': 'MyCrqEnv'], repoSlug: 'Org/Repo')

                def plugin = new CrqPlugin()
                Closure crqClosure = plugin.addCrq('myEnv')
                Closure pipeline = mock(Closure.class)

                crqClosure.delegate = pipeline
                crqClosure.resolveStrategy = Closure.DELEGATE_FIRST
                crqClosure.call(pipeline)

                verify(pipeline).call()
            }

            // @Test shouldCallRemedierOpen
        }

        public class withoutCrqEnvironment {
            @Test
            public void shouldExecutePipeline() {
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

    public class GetCrqEnviroment {
        @Test
        void returnsCrqEnvirommentIfPresent() {
            def plugin = new CrqPlugin()
            String expectedCrqEnvironment = 'someEnvironment'

            configureJenkins(env: ['CRQ_ENVIRONMENT': expectedCrqEnvironment])

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(expectedCrqEnvironment))
        }

        @Test
        void returnsEnvironmentSpecificCrqEnvirommentIfPresent() {
            def plugin = new CrqPlugin()
            String expectedCrqEnvironment = 'someEnvironment'

            configureJenkins(env: ['MYENV_CRQ_ENVIRONMENT': expectedCrqEnvironment])

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(expectedCrqEnvironment))
        }

        @Test
        void prefersNonPrefixedCrqOverPrefixedCrq() {
            // The UseCase:
            //     Set PROD_CRQ_ENVIRONMENT as a global variable, to make multiple pipeline's prod behave the same way
            //     Set CRQ_ENVIRONMENT in a specific app's prod environment (eg: parameterstore), to override the global value
            def plugin = new CrqPlugin()
            String nonPrefixedCrq = 'nonPrefixed'
            String prefixedCrq = 'prefixed'

            configureJenkins(env: [
                'CRQ_ENVIRONMENT': nonPrefixedCrq,
                'MYENV_CRQ_ENVIRONMENT': prefixedCrq
            ])

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(nonPrefixedCrq))
        }

    }
}

