import static TerraformEnvironmentStage.PLAN_COMMAND
import static TerraformEnvironmentStage.APPLY_COMMAND

class PassPlanFilePlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin, TerraformEnvironmentStagePlugin {

    public static void init() {
        PassPlanFilePlugin plugin = new PassPlanFilePlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN_COMMAND,  stashPlan(stage.getEnvironment()))
        stage.decorate(APPLY_COMMAND, unstashPlan(stage.getEnvironment()))
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        String env = command.getEnvironment()
        command.withArgument("-out=tfplan-" + env)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        String env = command.getEnvironment()
        command.withDirectory("tfplan-" + env)
    }

    public Closure stashPlan(String env) {
        return { closure ->
            closure()
            String planFile = "tfplan-" + env
            echo "Stashing ${planFile} file"
            stash name: planFile, includes: planFile
        }
    }

    public Closure unstashPlan(String env) {
        return { closure ->
            String planFile = "tfplan-" + env
            echo "Unstashing ${planFile} file"
            unstash planFile
            closure()
        }
    }

}
