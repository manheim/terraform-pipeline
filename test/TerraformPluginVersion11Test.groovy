import static org.hamcrest.Matchers.containsString
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformPluginVersion11Test {
    @Nested
    class ModifiesTerraformValidateCommand {
        @Test
        void addsCheckVariablesFalseToValidateCommand() {
            def validateCommand = spy(new TerraformValidateCommand())
            def version11 = new TerraformPluginVersion11()

            version11.apply((TerraformValidateCommand)validateCommand)

            verify(validateCommand).withArgument('-check-variables=false')
        }
    }

    @Nested
    class ModifiesTerraformPlanCommand {
        @Test
        void toPreserveTerraform11CliSyntaxForVariables() {
            def plan = new TerraformPlanCommand()
            def version11 = new TerraformPluginVersion11()

            version11.apply(plan)
            plan.withVariable('key', 'value')
            def result = plan.toString()

            assertThat(result, containsString("-var 'key=value'"))
        }

        @Test
        void toPreserveTerraform11CliFormatForMapVariables() {
            def plan = new TerraformPlanCommand()
            def version11 = new TerraformPluginVersion11()

            version11.apply(plan)
            plan.withVariable('myMap', [key1:'value1', key2: 'value2'])
            def result = plan.toString()

            assertThat(result, containsString("-var 'myMap={key1=\"value1\",key2=\"value2\"}'"))
        }
    }

    @Nested
    class ModifiesTerraformApplyCommand {
        @Test
        void toPreserveTerraform11CliSyntaxForVariables() {
            def applyCommand = new TerraformApplyCommand()
            def version11 = new TerraformPluginVersion11()

            version11.apply(applyCommand)
            applyCommand.withVariable('key', 'value')
            def result = applyCommand.toString()

            assertThat(result, containsString("-var 'key=value'"))
        }

        @Test
        void toPreserveTerraform11CliFormatForMapVariables() {
            def applyCommand = new TerraformPlanCommand()
            def version11 = new TerraformPluginVersion11()

            version11.apply(applyCommand)
            applyCommand.withVariable('myMap', [key1:'value1', key2: 'value2'])
            def result = applyCommand.toString()

            assertThat(result, containsString("-var 'myMap={key1=\"value1\",key2=\"value2\"}'"))
        }
    }
}

