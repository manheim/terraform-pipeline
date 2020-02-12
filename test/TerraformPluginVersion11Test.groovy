import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify;

import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformPluginVersion11Test {

    class ModifiesTerraformValidateCommand {
        @Test
        void addsCheckVariablesFalseToValidateCommand() {
            def validateCommand = spy(new TerraformValidateCommand())
            def version11 = new TerraformPluginVersion11()

            version11.apply((TerraformValidateCommand)validateCommand)

            verify(validateCommand).withArgument('-check-variables=false')
        }
    }
}

