class PlanOnlyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public Closure skipApply() {
        return  { closure ->
            echo "Skipping apply stage. PlanOnlyPlugin is enabled."
        }
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorateAround(APPLY, skipApply())
    }
}
