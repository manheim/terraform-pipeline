import static org.hamcrest.Matchers.containsString
import static org.junit.Assert.assertThat
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

    class ModifiesTerraformPlanCommand {
        @Test
        void toPreserveTerraform11CliSyntaxForVariables() {
            def plan = new TerraformPlanCommand()
            def version12 = new TerraformPluginVersion11()

            version12.apply(plan)
            plan.withVariable('key', 'value')
            def result = plan.toString()

            assertThat(result, containsString("-var 'key=value'"))
        }
    }

    class ModifiesTerraformApplyCommand {
        @Test
        void toPreserveTerraform11CliSyntaxForVariables() {
            def applyCommand = new TerraformApplyCommand()
            def version12 = new TerraformPluginVersion11()

            version12.apply(applyCommand)
            applyCommand.withVariable('key', 'value')
            def result = applyCommand.toString()

            assertThat(result, containsString("-var 'key=value'"))
        }
    }
}

