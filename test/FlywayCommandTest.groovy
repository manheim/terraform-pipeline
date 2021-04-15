import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class FlywayCommandTest {
    @Nested
    public class WithUser {
        @Test
        void isfluent() {
            def result = FlywayCommand.withUser('someUser')

            assertThat(result, equalTo(FlywayCommand.class))
        }
    }

    @Nested
    public class WithPassword {
        @Test
        void isfluent() {
            def result = FlywayCommand.withPassword('somePassword')

            assertThat(result, equalTo(FlywayCommand.class))
        }
    }

    @Nested
    public class WithLocations {
        @Test
        void isFluent() {
            def result = FlywayCommand.withLocations('someLocations')

            assertThat(result, equalTo(FlywayCommand.class))
        }
    }

    @Nested
    public class WithUrl {
        @Test
        void isFluent() {
            def result = FlywayCommand.withUrl('someUrl')

            assertThat(result, equalTo(FlywayCommand.class))
        }
    }

    @Nested
    public class WithAdditionalParameter {
        @Test
        void isFluent() {
            def result = FlywayCommand.withAdditionalParameter('-someParam=someValue')

            assertThat(result, equalTo(FlywayCommand.class))
        }
    }

    @Nested
    public class ToString {
        @Test
        void constructsTheCommand() {
            def command = new FlywayCommand("info")

            def result = command.toString()

            assertThat(result, equalTo("flyway info"))
        }

        @Nested
        public class WithUser {
            @Test
            void includesTheUserParameter() {
                def expectedUser = 'someUser'
                FlywayCommand.withUser(expectedUser)
                def command = new FlywayCommand('blah')

                def result = command.toString()

                assertThat(result, containsString("-user=${expectedUser}"))
            }
        }

        @Nested
        public class WithPassword {
            @Test
            void includesThePasswordParameter() {
                def expectedPassword = 'somePassword'
                FlywayCommand.withPassword(expectedPassword)
                def command = new FlywayCommand('blah')

                def result = command.toString()

                assertThat(result, containsString("-password=${expectedPassword}"))
            }
        }

        @Nested
        public class WithLocations {
            @Test
            void includesTheLocationsParameter() {
                def expectedLocations = 'filesystem:/some/dir'
                FlywayCommand.withLocations(expectedLocations)
                def command = new FlywayCommand('blah')

                def result = command.toString()

                assertThat(result, containsString("-locations=${expectedLocations}"))
            }
        }

        @Nested
        public class WithUrl {
            @Test
            void includesTheUrlParameter() {
                def expectedUrl = 'jdbc:mysql://someurl'
                FlywayCommand.withUrl(expectedUrl)
                def command = new FlywayCommand('blah')

                def result = command.toString()

                assertThat(result, containsString("-url=${expectedUrl}"))
            }
        }

        @Nested
        public class WithAdditionalParameters {
            @Test
            void addsTheAdditionalParameter() {
                def expectedParameter = "-someParam=someValue"
                FlywayCommand.withAdditionalParameter(expectedParameter)
                def command = new FlywayCommand('blah')

                def result = command.toString()

                assertThat(result, containsString(expectedParameter))
            }

            @Test
            void addsMultipleAdditionalParameters() {
                def param1 = '-param1=value1'
                def param2 = '-param2=value2'
                FlywayCommand.withAdditionalParameter(param1)
                             .withAdditionalParameter(param2)
                def command = new FlywayCommand('blah')

                def result = command.toString()

                assertThat(result, containsString(param1))
                assertThat(result, containsString(param2))
            }
        }
    }
}
