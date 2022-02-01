import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class TerraformPluginVersion15Test {
    @Nested
    class ModifiesTerraformValidateCommand {
        @Test
        void toUseTerraform15CliSyntaxForDirectory() {
            def validate = new TerraformValidateCommand()
            def version15 = new TerraformPluginVersion15()

            version15.apply(validate)
            validate.withDirectory('foobar')
            def result = validate.toString()

            assertThat(result, containsString(" -chdir=foobar"))
        }
    }

    @Nested
    class ModifiesTerraformInitCommand {
        @Test
        void toUseTerraform15CliSyntaxForDirectory() {
            def init = new TerraformInitCommand()
            def version15 = new TerraformPluginVersion15()

            version15.apply(init)
            init.withDirectory('foobar')
            def result = init.toString()

            assertThat(result, containsString(" -chdir=foobar"))
        }
    }

    @Nested
    class ModifiesTerraformPlanCommand {
        @Test
        void toUseTerraform15CliSyntaxForDirectory() {
            def plan = new TerraformPlanCommand()
            def version15 = new TerraformPluginVersion15()

            version15.apply(plan)
            plan.withDirectory('foobar')
            def result = plan.toString()

            assertThat(result, containsString(" -chdir=foobar"))
        }
    }

    @Nested
    class ModifiesTerraformApplyCommand {
        @Test
        void toUseTerraform15CliSyntaxForDirectory() {
            def apply = new TerraformApplyCommand()
            def version15 = new TerraformPluginVersion15()

            version15.apply(apply)
            apply.withDirectory('foobar')
            def result = apply.toString()

            assertThat(result, containsString(" -chdir=foobar"))
        }
    }


}

