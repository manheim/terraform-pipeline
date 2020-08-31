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
        String env = command.getEnvironment()
        command.withArgument("-out=tfplan-" + env)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        command.withArgument(planAbsolutePath)
    }

    public Closure savePlanFile(String env) {
        return { closure ->
            closure()
            String workingDir = pwd()
            setAbsolutePath(workingDir, env)
        }
    }

    public void setAbsolutePath(String workingDir, String env) {
        this.planAbsolutePath = workingDir + "/tfplan-" + env
    }


    public static void reset() {
        planAbsolutePath = null
    }
}
