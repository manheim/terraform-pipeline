import static TerraformEnvironmentStage.ALL

class PlanOnlyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()
        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformEnvironmentStage.addParam([
            $class: 'hudson.model.BooleanParameterDefinition',
            name: "FAIL_PLAN_ON_CHANGES",
            default: true,
            description: 'Plan run with -detailed-exitcode; ANY CHANGES will cause failure'
        ])
        TerraformEnvironmentStage.addParam([
            $class: 'hudson.model.BooleanParameterDefinition',
            name: "PLAN_ONLY",
            default: false,
            description: "only run a plan, no apply, even if on master"
        ])
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new PlanOnlyStrategy())
    }
}
