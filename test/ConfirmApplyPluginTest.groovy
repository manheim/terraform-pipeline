import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class ConfirmApplyPluginTest {
    @Test
    void modifiesTerraformEnvironmentStageByDefault() {
        Collection actualPlugins = TerraformEnvironmentStage.getPlugins()

        assertThat(actualPlugins, hasItem(instanceOf(ConfirmApplyPlugin.class)))
    }

    @Test
    void ConfirmApplyPluginEnabled() {
        ConfirmApplyPlugin.enable()

        assertTrue(ConfirmApplyPlugin.enabled)
    }

    @Test
    void ConfirmApplyPluginDisabled() {
        ConfirmApplyPlugin.disable()

        assertFalse(ConfirmApplyPlugin.enabled)
    }

    @Nested
    class GetInputOptions {
        @Test
        void defaultsToASubmitterParameterOfApprover() {
            def plugin = new ConfirmApplyPlugin()

            def actual = plugin.getInputOptions('env')['submitterParameter']

            assertEquals(ConfirmApplyPlugin.DEFAULT_SUBMITTER_PARAMETER, actual)
        }

        @Test
        void usesTheGivenSubmitterParameter() {
            def expected = 'submitterOverride'
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withSubmitterParameter(expected)

            def actual = plugin.getInputOptions('env')['submitterParameter']

            assertEquals(expected, actual)
        }

        @Test
        void defaultsToNoExtraParameters() {
            def plugin = new ConfirmApplyPlugin()

            def parameters = plugin.getInputOptions('env')['parameters']

            assertNull(parameters)
        }

        @Test
        void addsTheParameterToConfirmationInputOptions() {
            def expectedParameter = ['someParameterKey': 'someParameterValue']
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withParameter(expectedParameter)

            def parameters = plugin.getInputOptions('env')['parameters']

            assertThat(parameters, contains(expectedParameter))
        }

        @Test
        void interpolatesEnvironmentVariableInConfirmMessage() {
            def environment = 'foo'
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withConfirmMessage('confirm for ${environment}')

            def message = plugin.getInputOptions(environment)['message']

            assertEquals("confirm for foo", message)
        }

        @Test
        void interpolatesEnvironmentVariableInAddParameters() {
            def environment = 'foo'
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withParameter([
                name: 'confirm',
                description: 'confirm for ${environment}'
            ])

            def description = plugin.getInputOptions(environment)['parameters'][0]['description']

            assertEquals("confirm for foo", description)
        }
    }

    @Nested
    class CheckConfirmConditions {
        @Test
        void doesNothingByDefault() {
            def plugin = new ConfirmApplyPlugin()

            plugin.checkConfirmConditions('someInput', 'foo')
        }

        @Test
        void doesNothingIfAllConditionsReturnTrue() {
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withConfirmCondition { options -> true }
                              .withConfirmCondition { options -> true }
                              .withConfirmCondition { options -> true }

            plugin.checkConfirmConditions('someInput', 'foo')
        }

        @Test
        void raisesAnExceptionIfAnyConditionReturnsFalse() {
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withConfirmCondition { options -> true }
                              .withConfirmCondition { options -> false }
                              .withConfirmCondition { options -> true }

            assertThrows(RuntimeException.class) {
                plugin.checkConfirmConditions('someInput', 'foo')
            }
        }

        @Test
        void passesTheCorrectEnvironmentToConditions() {
            def expectedEnvironment = 'foo'
            def actualEnvironment
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withConfirmCondition { options -> actualEnvironment = options['environment'] }

            plugin.checkConfirmConditions('someInput', expectedEnvironment)

            assertEquals(expectedEnvironment, actualEnvironment)
        }

        @Test
        void passesTheCorrectUserInput() {
            def expectedUserInput = 'someInput'
            def actualUserInput
            def plugin = new ConfirmApplyPlugin()
            ConfirmApplyPlugin.withConfirmCondition { options -> actualUserInput = options['input'] }

            plugin.checkConfirmConditions(expectedUserInput, 'someEnv')

            assertEquals(expectedUserInput, actualUserInput)
        }
    }

    @Nested
    class WithParameter {
        @Test
        void isFluent() {
            def result = ConfirmApplyPlugin.withParameter([:])

            assertEquals(ConfirmApplyPlugin.class, result)
        }
    }

    @Nested
    class WithConfirmMessage {
        @Test
        void isFluent() {
            def result = ConfirmApplyPlugin.withConfirmMessage('someMessage')

            assertEquals(ConfirmApplyPlugin.class, result)
        }
    }

    @Nested
    class WithOkMessage {
        @Test
        void isFluent() {
            def result = ConfirmApplyPlugin.withOkMessage('someMessage')

            assertEquals(ConfirmApplyPlugin.class, result)
        }
    }

    @Nested
    class WithSubmitterParameter {
        @Test
        void isFluent() {
            def result = ConfirmApplyPlugin.withSubmitterParameter('someParam')

            assertEquals(ConfirmApplyPlugin.class, result)
        }
    }
}

