import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.startsWith

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformFormatCommandTest {
    @Nested
    public class ToString {
        @Test
        void includesTerraformFormatCommand() {
            def command = new TerraformFormatCommand()

            def actual = command.toString()

            assertThat(actual, startsWith('terraform fmt'))
        }

        @Nested
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

            @Nested
            public class WithPatternOverride {
                @Test
                void usesThePatternWhenCheckIsFalse() {
                    def command = new TerraformFormatCommand()
                    TerraformFormatCommand.withCheck(false)

                    command.withCheckOptionPattern { "valueFor(${it})" }
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(false)'))
                }

                @Test
                void usesThePatternWhenCheckIsTrue() {
                    def command = new TerraformFormatCommand()
                    TerraformFormatCommand.withCheck(true)

                    command.withCheckOptionPattern { "valueFor(${it})" }
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(true)'))
                }
            }
        }

        @Nested
        public class WithRecursive {
            @Test
            void doesNothingByDefaultUnsupportedByTerraformm11() {
                def command = new TerraformFormatCommand()

                TerraformFormatCommand.withRecursive()
                def actual = command.toString()

                assertThat(actual, equalTo('terraform fmt'))
            }

            @Nested
            public class WithPatternOverride {
                @Test
                void usesThePatternWhenCheckIsFalse() {
                    def command = new TerraformFormatCommand()
                    TerraformFormatCommand.withRecursive(false)

                    command.withRecursiveOptionPattern { "valueFor(${it})" }
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(false)'))
                }

                @Test
                void usesThePatternWhenCheckIsTrue() {
                    def command = new TerraformFormatCommand()
                    TerraformFormatCommand.withRecursive(true)

                    command.withRecursiveOptionPattern { "valueFor(${it})" }
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(true)'))
                }
            }
        }

        @Nested
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

            @Nested
            public class WithPatternOverride {
                @Test
                void usesThePatternWhenCheckIsFalse() {
                    def command = new TerraformFormatCommand()
                    TerraformFormatCommand.withDiff(false)

                    command.withDiffOptionPattern { "valueFor(${it})" }
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(false)'))
                }

                @Test
                void usesThePatternWhenCheckIsTrue() {
                    def command = new TerraformFormatCommand()
                    TerraformFormatCommand.withDiff(true)

                    command.withDiffOptionPattern { "valueFor(${it})" }
                    def actual = command.toString()

                    assertThat(actual, containsString('valueFor(true)'))
                }
            }
        }
    }

    @Nested
    public class WithCheck {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withCheck()

            assertThat(result, equalTo(TerraformFormatCommand.class))
        }
    }

    @Nested
    public class WithRecursive {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withRecursive()

            assertThat(result, equalTo(TerraformFormatCommand.class))
        }
    }

    @Nested
    public class WithDiff {
        @Test
        void isFluent() {
            def result = TerraformFormatCommand.withDiff()

            assertThat(result, equalTo(TerraformFormatCommand.class))
        }
    }

    @Nested
    public class WithCheckOptionPattern {
        @Test
        void isFluent() {
            def command = new TerraformFormatCommand()
            def result = command.withCheckOptionPattern { "somevalue" }

            assertThat(result, equalTo(command))
        }
    }

    @Nested
    public class WithRecursiveOptionPattern {
        @Test
        void isFluent() {
            def command = new TerraformFormatCommand()
            def result = command.withRecursiveOptionPattern { 'somevalue' }

            assertThat(result, equalTo(command))
        }
    }

    @Nested
    public class WithDiffOptionPattern {
        @Test
        void isFluent() {
            def command = new TerraformFormatCommand()
            def result = command.withDiffOptionPattern { 'somevalue' }

            assertThat(result, equalTo(command))
        }
    }
}
