import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class FlywayCommandTest {
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
    public class ToString {
        @Test
        void constructsTheCommand() {
            def command = new FlywayCommand("info")

            def result = command.toString()

            assertThat(result, equalTo("flyway info"))
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
    }
}
