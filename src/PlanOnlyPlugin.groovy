import static TerraformEnvironmentStage.ALL

class PlanOnlyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new PlanOnlyStrategy())
        stage.decorate(ALL, addBuildParams())
    }

    public static Closure addBuildParams() {
        return { closure ->

            def existing_params = currentBuild.rawBuild.parent.properties
                .findAll { it.value instanceof hudson.model.ParametersDefinitionProperty }
                .collectMany { it.value.parameterDefinitions }

            def params = [
                booleanParam(name: 'FAIL_PLAN_ON_CHANGES', defaultValue: true, description: 'Plan run with -detailed-exitcode; ANY CHANGES will cause failure'),
            ] + existing_params

            def props = [
                parameters(params)
            ]
            properties(props)

            closure()
        }
    }

}
