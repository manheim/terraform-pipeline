class PlanOnlyPlugin implements TerraformPlanCommandPlugin, TerraformEnvironmentStagePlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformEnvironmentStage.addParam([
            $class: 'hudson.model.BooleanParameterDefinition',
            name: "FAIL_PLAN_ON_CHANGES",
            defaultValue: false,
            description: 'Plan run with -detailed-exitcode; ANY CHANGES will cause failure'
        ])
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        // Jenkins boolen env vars get converted to strings
        if (Jenkinsfile.instance.getEnv().FAIL_PLAN_ON_CHANGES == 'true') {
            command.withArgument('-detailed-exitcode')
        }
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new PlanOnlyStrategy())
    }
}
