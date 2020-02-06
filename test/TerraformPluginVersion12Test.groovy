import static org.hamcrest.Matchers.containsString
import static org.junit.Assert.assertThat
import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify;

import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

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

