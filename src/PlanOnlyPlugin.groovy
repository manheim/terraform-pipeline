import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.CONFIRM

class PlanOnlyPlugin implements TerraformEnvironmentStagePlugin, TerraformPlanCommandPlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public Closure skipStage(String stageName) {
        return  { closure ->
            echo "Skipping ${stageName} stage. PlanOnlyPlugin is enabled."
        }
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorateAround(CONFIRM, skipStage(CONFIRM))
        stage.decorateAround(APPLY, skipStage(APPLY))
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withPrefix('set -e; set -o pipefail;')
        command.withArgument('-detailed-exitcode')
    }
}
