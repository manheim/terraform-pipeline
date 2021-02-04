import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TerraformPluginVersion12Test {
    @BeforeEach
    @AfterEach
    void reset() {
        TerraformFormatCommand.reset()
    }

    @Nested
    class AddInitBefore {
        @Test
        void runsTheInnerClosure() {
            def plugin = new TerraformPluginVersion12()
            def wasRun = false
            def innerClosure = { wasRun = true }

            def beforeClosure = plugin.addInitBefore()
            beforeClosure.delegate = new DummyJenkinsfile()
            beforeClosure(innerClosure)

            assertThat(wasRun, is(true))
        }
    }

    @Nested
    class InitCommandForValidate {
        @Test
        void createsCommandWithNoBackend() {
            def initCommand = new TerraformPluginVersion12().initCommandForValidate()

            assertThat(initCommand.toString(), containsString('-backend=false'))
        }
    }

    @Nested
    class ModifiesTerraformValidateStage {
        @Test
        void addsTerraformInitBeforeValidate()  {
            def validateStage = spy(new TerraformValidateStage())
            def version12 = new TerraformPluginVersion12()

            version12.apply(validateStage)

            verify(validateStage).decorate(eq(TerraformValidateStage.VALIDATE), any())
        }
    }

    @Nested
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

    @Nested
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

    @Nested
    class ModifiesTerraformFormatCommand {
        @Nested
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

        @Nested
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

        @Nested
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

