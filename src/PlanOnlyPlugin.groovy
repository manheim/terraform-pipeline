import static TerraformEnvironmentStage.ALL

class PlanOnlyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()
        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformEnvironmentStage.addParam([
            $class: 'hudson.model.BooleanParameterDefinition',
            name: "FAIL_PLAN_ON_CHANGES",
            defaultValue: false,
            description: 'Plan run with -detailed-exitcode; ANY CHANGES will cause failure'
        ])
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new PlanOnlyStrategy())
    }
}
