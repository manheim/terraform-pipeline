import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class CrqPluginTest {
    public class Init {
        @After
        void resetPlugins() {
            TerraformEnvironmentStage.reset()
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
                def plugin = spy(new CrqPlugin())
                when(plugin.getEnv()).thenReturn(['CRQ_ENVIRONMENT': 'MyCrqEnv'])
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
            String expectedCrqEnvironment = 'someEnvironment'
            CrqPlugin plugin = spy(new CrqPlugin())
            when(plugin.getEnv()).thenReturn(['CRQ_ENVIRONMENT': expectedCrqEnvironment])

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(expectedCrqEnvironment))
        }

        @Test
        void returnsEnvironmentSpecificCrqEnvirommentIfPresent() {
            String expectedCrqEnvironment = 'someEnvironment'
            CrqPlugin plugin = spy(new CrqPlugin())
            when(plugin.getEnv()).thenReturn(['MYENV_CRQ_ENVIRONMENT': expectedCrqEnvironment])

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(expectedCrqEnvironment))
        }

        @Test
        void prefersNonPrefixedCrqOverPrefixedCrq() {
            // The UseCase:
            //     Set PROD_CRQ_ENVIRONMENT as a global variable, to make multiple pipeline's prod behave the same way
            //     Set CRQ_ENVIRONMENT in a specific app's prod environment (eg: parameterstore), to override the global value
            CrqPlugin plugin = spy(new CrqPlugin())
            String nonPrefixedCrq = 'nonPrefixed'
            String prefixedCrq = 'prefixed'

            when(plugin.getEnv()).thenReturn([
                'CRQ_ENVIRONMENT': nonPrefixedCrq,
                'MYENV_CRQ_ENVIRONMENT': prefixedCrq
            ])

            String actualCrqEnvironment = plugin.getCrqEnvironment('myenv')

            assertThat(actualCrqEnvironment, is(nonPrefixedCrq))
        }

    }
}

