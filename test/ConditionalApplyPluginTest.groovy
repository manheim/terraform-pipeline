import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class ConditionalApplyPluginTest {
    @Test
    void modifiesTerraformEnvironmentStageByDefault() {
        Collection actualPlugins = TerraformEnvironmentStage.getPlugins()

        assertThat(actualPlugins, hasItem(instanceOf(ConditionalApplyPlugin.class)))
    }

    @Nested
    class WithApplyOnBranch {
        @Test
        public void isFluent() {
            def result = ConditionalApplyPlugin.withApplyOnBranch('someBranch')

            assertEquals(ConditionalApplyPlugin.class, result)
        }
    }

    @Nested
    class WithApplyOnEnvironment {
        @Test
        public void isFluent() {
            def result = ConditionalApplyPlugin.withApplyOnEnvironment('someEnv')

            assertEquals(ConditionalApplyPlugin.class, result)
        }
    }

    @Nested
    class ShouldApply {
        @Test
        void returnsTrueForMainByDefault() {
            MockJenkinsfile.withEnv(BRANCH_NAME: 'main')
            def plugin = new ConditionalApplyPlugin()

            assertTrue(plugin.shouldApply())
        }

        @Test
        void returnsFalseForNonMainByDefault() {
            MockJenkinsfile.withEnv(BRANCH_NAME: 'notMain')
            def plugin = new ConditionalApplyPlugin()

            assertFalse(plugin.shouldApply())
        }

        @Test
        void returnsTrueWhenBranchIsUnknown() {
            MockJenkinsfile.withEnv()
            def plugin = new ConditionalApplyPlugin()

            assertTrue(plugin.shouldApply())
        }

        @Nested
        class WithApplyOnBranchEnabled {
            @Test
            void returnsTrueForFirstConfiguredBranch() {
                MockJenkinsfile.withEnv(BRANCH_NAME: 'qa')
                ConditionalApplyPlugin.withApplyOnBranch('qa', 'someOtherBranch')
                def plugin = new ConditionalApplyPlugin()

                assertTrue(plugin.shouldApply())
            }

            @Test
            void returnsTrueForOtherConfiguredBranches() {
                MockJenkinsfile.withEnv(BRANCH_NAME: 'someOtherBranch')
                ConditionalApplyPlugin.withApplyOnBranch('qa', 'someOtherBranch')
                def plugin = new ConditionalApplyPlugin()

                assertTrue(plugin.shouldApply())
            }

            @Test
            void returnsFalseForNonMatchingBranch() {
                MockJenkinsfile.withEnv(BRANCH_NAME: 'notQa')
                ConditionalApplyPlugin.withApplyOnBranch('qa', 'someOtherBranch')
                def plugin = new ConditionalApplyPlugin()

                assertFalse(plugin.shouldApply())
            }
        }

        @Nested
        class WithApplyOnEnvironmentEnabled {
            @Nested
            class OnMainBranch {
                @Test
                void returnsTrueForTheGivenEvironment() {
                    MockJenkinsfile.withEnv(BRANCH_NAME: 'main')
                    ConditionalApplyPlugin.withApplyOnEnvironment('qa')
                    def plugin = new ConditionalApplyPlugin()

                    assertTrue(plugin.shouldApply('qa'))
                }

                @Test
                void returnsTrueForOtherEnvironments() {
                    MockJenkinsfile.withEnv(BRANCH_NAME: 'main')
                    ConditionalApplyPlugin.withApplyOnEnvironment('qa')
                    def plugin = new ConditionalApplyPlugin()

                    assertTrue(plugin.shouldApply('notQa'))
                }
            }

            @Nested
            class OnNonMainBranch {
                @Test
                void returnsTrueForTheGivenEvironment() {
                    MockJenkinsfile.withEnv(BRANCH_NAME: 'notMain')
                    ConditionalApplyPlugin.withApplyOnEnvironment('qa')
                    def plugin = new ConditionalApplyPlugin()

                    assertTrue(plugin.shouldApply('qa'))
                }

                @Test
                void returnsFalseForOtherEnvironments() {
                    MockJenkinsfile.withEnv(BRANCH_NAME: 'notMain')
                    ConditionalApplyPlugin.withApplyOnEnvironment('qa')
                    def plugin = new ConditionalApplyPlugin()

                    assertFalse(plugin.shouldApply('notQa'))
                }
            }
        }

        @Nested
        class WhenDisabled {
            @Test
            void returnsTrueWhenBranchIsMain() {
                ConditionalApplyPlugin.disable()
                MockJenkinsfile.withEnv(BRANCH_NAME: 'main')
                def plugin = new ConditionalApplyPlugin()

                assertTrue(plugin.shouldApply())
            }

            void returnsTrueWhenBranchIsAnythingOtherThanMain() {
                ConditionalApplyPlugin.disable()
                MockJenkinsfile.withEnv(BRANCH_NAME: 'anyPossibleBranch')
                def plugin = new ConditionalApplyPlugin()

                assertTrue(plugin.shouldApply())
            }

            void returnsTrueWhenBranchIsUnknown() {
                ConditionalApplyPlugin.disable()
                MockJenkinsfile.withEnv()
                def plugin = new ConditionalApplyPlugin()

                assertTrue(plugin.shouldApply())
            }
        }
    }
}

