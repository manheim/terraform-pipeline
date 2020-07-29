import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.CONFIRM

class PlanOnlyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()

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
}
