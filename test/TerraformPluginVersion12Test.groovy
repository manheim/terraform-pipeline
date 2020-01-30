import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.*

@RunWith(HierarchicalContextRunner.class)
class TerraformPluginVersion12Test {

    class InitCommandForValidate {
        @Test
        void createsCommandWithNoBackend() {
            def initCommand = new TerraformPluginVersion12().initCommandForValidate()

            assertThat(initCommand.toString(), containsString('-backend=false'))
        }
    }

    class ModifiesTerraformValidateStage {
        @Test
        void addsTerraformInitBeforeValidate()  {
            def validateStage = spy(new TerraformValidateStage())
            def version12 = new TerraformPluginVersion12()

            version12.apply(validateStage)

            verify(validateStage).decorate(eq(TerraformValidateStage.VALIDATE), any())
        }
    }
}

