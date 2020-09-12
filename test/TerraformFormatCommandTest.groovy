import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.startsWith

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class TerraformFormatCommandTest {
    @Before
    @After
    public void reset() {
        TerraformFormatCommand.reset()
    }

    public class ToString {
        @Test
        void includesTerraformFormatCommand() {
            def command = new TerraformFormatCommand()

            def actual = command.toString()

            assertThat(actual, startsWith('terraform fmt'))
        }

        public class WithCheck {
            @Test
            void addsCheckOptionByDefault() {
                def command = new TerraformFormatCommand()

                TerraformFormatCommand.withCheck()
                def actual = command.toString()

                assertThat(actual, containsString('-check=true'))
            }

            @Test
            void doesNotAddCheckOptionWhenFalse() {
                def command = new TerraformFormatCommand()

                TerraformFormatCommand.withCheck(false)
                def actual = command.toString()

                assertThat(actual, not(containsString('-check')))
            }

            public class WithPatternOverride {
                @Test
                void usesThePatternWhenCheckIsFalse() {
                    def command = new TerraformFormatCommand()

                    TerraformFormatCommand.withCheckOptionPattern { "valueFor(${it})" }
                    TerraformFormatCommand.withCheck(false)
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(false)'))
                }

                @Test
                void usesThePatternWhenCheckIsTrue() {
                    def command = new TerraformFormatCommand()

                    TerraformFormatCommand.withCheckOptionPattern { "valueFor(${it})" }
                    TerraformFormatCommand.withCheck(true)
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(true)'))
                }
            }
        }

        public class WithRecursive {
            @Test
            void doesNothingByDefaultUnsupportedByTerraformm11() {
                def command = new TerraformFormatCommand()

                TerraformFormatCommand.withRecursive()
                def actual = command.toString()

                assertEquals(actual, 'terraform fmt')
            }

            public class WithPatternOverride {
                @Test
                void usesThePatternWhenCheckIsFalse() {
                    def command = new TerraformFormatCommand()

                    TerraformFormatCommand.withRecursiveOptionPattern { "valueFor(${it})" }
                    TerraformFormatCommand.withRecursive(false)
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(false)'))
                }

                @Test
                void usesThePatternWhenCheckIsTrue() {
                    def command = new TerraformFormatCommand()

                    TerraformFormatCommand.withRecursiveOptionPattern { "valueFor(${it})" }
                    TerraformFormatCommand.withRecursive(true)
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(true)'))
                }
            }
        }

        public class WithDiff {
            @Test
            void addsDiffOptionByDefault() {
                def command = new TerraformFormatCommand()

                TerraformFormatCommand.withDiff()
                def actual = command.toString()

                assertThat(actual, containsString('-diff=true'))
            }

            @Test
            void doesNotAddDiffOptionWhenFalse() {
                def command = new TerraformFormatCommand()

                TerraformFormatCommand.withDiff(false)
                def actual = command.toString()

                assertThat(actual, not(containsString('-diff')))
            }

            public class WithPatternOverride {
                @Test
                void usesThePatternWhenCheckIsFalse() {
                    def command = new TerraformFormatCommand()

                    TerraformFormatCommand.withDiffOptionPattern { "valueFor(${it})" }
                    TerraformFormatCommand.withDiff(false)
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(false)'))
                }

                @Test
                void usesThePatternWhenCheckIsTrue() {
                    def command = new TerraformFormatCommand()

                    TerraformFormatCommand.withDiffOptionPattern { "valueFor(${it})" }
                    TerraformFormatCommand.withDiff(true)
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(true)'))
                }
            }
        }
    }

    public class WithCheck {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withCheck()

            assertEquals(result, TerraformFormatCommand.class)
        }
    }

    public class WithCheckOptionPattern {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withCheckOptionPattern { "somevalue" }

            assertEquals(result, TerraformFormatCommand.class)
        }
    }

    public class WithRecursive {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withRecursive()

            assertEquals(result, TerraformFormatCommand.class)
        }
    }

    public class WithRecursiveOptionPattern {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withRecursiveOptionPattern { 'somevalue' }

            assertEquals(TerraformFormatCommand.class, result)
        }
    }

    public class WithDiff {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withDiff()

            assertEquals(result, TerraformFormatCommand.class)
        }
    }

    public class WithDiffOptionPattern {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withDiffOptionPattern { 'somevalue' }

            assertEquals(TerraformFormatCommand.class, result)
        }
    }
}
