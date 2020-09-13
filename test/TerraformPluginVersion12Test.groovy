import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat
import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify;

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformPluginVersion12Test {
    @Before
    @After
    void reset() {
        TerraformFormatCommand.reset()
    }

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

    class ModifiesTerraformPlanCommand {
        @Test
        void toUseTerraform12CliSyntaxForVariables() {
            def plan = new TerraformPlanCommand()
            def version12 = new TerraformPluginVersion12()

            version12.apply(plan)
            plan.withVariable('key', 'value')
            def result = plan.toString()

            assertThat(result, containsString("-var='key=value'"))
        }

        @Test
        void toUseTerraform12CliFormatForMapVariables() {
            def plan = new TerraformPlanCommand()
            def version12 = new TerraformPluginVersion12()

            version12.apply(plan)
            plan.withVariable('myMap', [key1:'value1', key2: 'value2'])
            def result = plan.toString()

            assertThat(result, containsString("-var='myMap={\"key1\":\"value1\",\"key2\":\"value2\"}'"))
        }
    }

    class ModifiesTerraformApplyCommand {
        @Test
        void toUseTerraform12CliSyntaxForVariables() {
            def applyCommand = new TerraformApplyCommand()
            def version12 = new TerraformPluginVersion12()

            version12.apply(applyCommand)
            applyCommand.withVariable('key', 'value')
            def result = applyCommand.toString()

            assertThat(result, containsString("-var='key=value'"))
        }

        @Test
        void toUseTerraform12CliFormatForMapVariables() {
            def applyCommand = new TerraformApplyCommand()
            def version12 = new TerraformPluginVersion12()

            version12.apply(applyCommand)
            applyCommand.withVariable('myMap', [key1:'value1', key2: 'value2'])
            def result = applyCommand.toString()

            assertThat(result, containsString("-var='myMap={\"key1\":\"value1\",\"key2\":\"value2\"}'"))
        }
    }

    class ModifiesTerraformFormatCommand {
        class WithCheck {
            @Test
            void usesCheckFlagWhenCheckIsEnabled() {
                def formatCommand = new TerraformFormatCommand()
                def version12 = new TerraformPluginVersion12()

                TerraformFormatCommand.withCheck(true)
                version12.apply(formatCommand)
                def result = formatCommand.toString()

                assertThat(result, endsWith('-check'))
            }

            @Test
            void doesNotIncludeCheckFlagIfSetToFalse() {
                def formatCommand = new TerraformFormatCommand()
                def version12 = new TerraformPluginVersion12()

                TerraformFormatCommand.withCheck(false)
                version12.apply(formatCommand)
                def result = formatCommand.toString()

                assertThat(result, not(containsString('-check')))
            }
        }

        class WithRecursive {
            @Test
            void usesRecursiveFlagWhenRecursiveIsEnabled() {
                def formatCommand = new TerraformFormatCommand()
                def version12 = new TerraformPluginVersion12()

                TerraformFormatCommand.withRecursive(true)
                version12.apply(formatCommand)
                def result = formatCommand.toString()

                assertThat(result, endsWith('-recursive'))
            }

            @Test
            void doesNotIncludeRecursiveFlagIfSetToFalse() {
                def formatCommand = new TerraformFormatCommand()
                def version12 = new TerraformPluginVersion12()

                TerraformFormatCommand.withRecursive(false)
                version12.apply(formatCommand)
                def result = formatCommand.toString()

                assertThat(result, not(containsString('-recursive')))
            }
        }

        class WithDiff {
            @Test
            void usesDiffFlagWhenDiffIsEnabled() {
                def formatCommand = new TerraformFormatCommand()
                def version12 = new TerraformPluginVersion12()

                TerraformFormatCommand.withDiff(true)
                version12.apply(formatCommand)
                def result = formatCommand.toString()

                assertThat(result, endsWith('-diff'))
            }

            @Test
            void doesNotIncludeDiffFlagIfSetToFalse() {
                def formatCommand = new TerraformFormatCommand()
                def version12 = new TerraformPluginVersion12()

                TerraformFormatCommand.withDiff(false)
                version12.apply(formatCommand)
                def result = formatCommand.toString()

                assertThat(result, not(containsString('-diff')))
            }
        }
    }
}

