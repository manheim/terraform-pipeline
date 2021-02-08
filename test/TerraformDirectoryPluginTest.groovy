import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformDirectoryPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformInitCommand() {
            TerraformDirectoryPlugin.init()

            Collection actualPlugins = TerraformInitCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformDirectoryPlugin.class)))
        }

        @Test
        void modifiesTerraformValidateCommand() {
            TerraformDirectoryPlugin.init()

            Collection actualPlugins = TerraformValidateCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformDirectoryPlugin.class)))
        }

        @Test
        void modifiesTerraformPlanCommand() {
            TerraformDirectoryPlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformDirectoryPlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            TerraformDirectoryPlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(TerraformDirectoryPlugin.class)))
        }
    }

    @Nested
    public class Apply {
        @Nested
        public class WithDirectoryProvided {
            @Test
            void addsDirectoryToTerraformInit() {
                TerraformDirectoryPlugin.withDirectory('./customDirectory/')
                def plugin = new TerraformDirectoryPlugin()
                TerraformInitCommand command = TerraformInitCommand.instanceFor("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./customDirectory/"))
            }

            @Test
            void addsDirectoryToTerraformValidate() {
                TerraformDirectoryPlugin.withDirectory('./customDirectory/')
                def plugin = new TerraformDirectoryPlugin()
                TerraformValidateCommand command = TerraformValidateCommand.instance()

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./customDirectory/"))
            }

            @Test
            void addsDirectoryToTerraformPlan() {
                TerraformDirectoryPlugin.withDirectory('./customDirectory/')
                def plugin = new TerraformDirectoryPlugin()
                TerraformPlanCommand command = TerraformPlanCommand.instanceFor("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./customDirectory/"))
            }

            @Test
            void addsDirectoryToTerraformApply() {
                TerraformDirectoryPlugin.withDirectory('./customDirectory/')
                def plugin = new TerraformDirectoryPlugin()
                TerraformApplyCommand command = TerraformApplyCommand.instanceFor("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./customDirectory/"))
            }
        }

        @Nested
        public class WithoutDirectoryProvided {
            @Test
            void addsDirectoryToTerraformInit() {
                def plugin = new TerraformDirectoryPlugin()
                TerraformInitCommand command = TerraformInitCommand.instanceFor("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./terraform/"))
            }

            @Test
            void addsDirectoryToTerraformValidate() {
                def plugin = new TerraformDirectoryPlugin()
                TerraformValidateCommand command = TerraformValidateCommand.instance()

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./terraform/"))
            }

            @Test
            void addsDirectoryToTerraformPlan() {
                def plugin = new TerraformDirectoryPlugin()
                TerraformPlanCommand command = TerraformPlanCommand.instanceFor("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./terraform/"))
            }

            @Test
            void addsDirectoryToTerraformApply() {
                def plugin = new TerraformDirectoryPlugin()
                TerraformApplyCommand command = TerraformApplyCommand.instanceFor("myEnv")

                plugin.apply(command)

                String result = command.toString()
                assertThat(result, containsString("./terraform/"))
            }
        }
    }
}
