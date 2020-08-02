public class BuildWithParametersPlugin implements BuildStagePlugin,
                                                  TerraformValidateStagePlugin,
                                                  TerraformEnvironmentStagePlugin,
                                                  RegressionStagePlugin {

    public static void init() {
        def plugin = new BuildWithParametersPlugin()

        BuildStage.addPlugin(plugin)
        TerraformValidateStage.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
        RegressionStage.addPlugin(plugin)
    }

    @Override
    public void apply(BuildStage stage) {
        applyToAllStages(stage)
    }

    @Override
    public void apply(TerraformValidateStage stage) {
        applyToAllStages(stage)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        applyToAllStages(stage)
    }

    @Override
    public void apply(RegressionStage stage) {
        applyToAllStages(stage)
    }

    private void applyToAllStages(DecoratableStage stage) {
        stage.decorate(addParameterToFirstStageOnly())
    }

    public Closure addParameterToFirstStageOnly() {
        return { innerClosure ->
            innerClosure()
        }
    }
}
