import static TerraformEnvironmentStage.PLAN
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class PassPlanFilePlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin, TerraformEnvironmentStagePlugin {

    private static String planAbsolutePath

    public static void init() {
        PassPlanFilePlugin plugin = new PassPlanFilePlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN, savePlanFile(stage.getEnvironment()))
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withArgument("-out=tfplan")
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        command.withArgument(planAbsolutePath)
    }

    public Closure savePlanFile(String env) {
        return { closure ->
            closure()

            String planAbsolutePath = pwd() + "/tfplan"
        }
    }

    public static void reset() {
        planAbsolutePath = null
    }
}
