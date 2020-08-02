public class BuildWithParametersPlugin implements BuildStagePlugin,
                                                  TerraformValidateStagePlugin,
                                                  TerraformEnvironmentStagePlugin,
                                                  RegressionStagePlugin {

    private static globalBuildParameters = []
    private static appliedOnce = false

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
            if (hasParameters() && !appliedOnce) {
                properties([
                    parameters(getParameters())
                ])
                appliedOnce = true
            }

            innerClosure()
        }
    }

    public static withBooleanParameter(Map options) {
        if (options['name'] == null) {
            throw new RuntimeException('A "name" option is required for BuildWithParametersPlugin.withBooleanParameter(). Your options: ${options.toString()}')
        }

        if (options['description'] == null) {
            throw new RuntimeException('A "description" option is required for BuildWithParametersPlugin.withBooleanParameter(). Your options: ${options.toString()}')
        }

        globalBuildParameters << options
    }

    /*
    public static withStringParameter(Map options) {
    }
    */

    public static withParameter(Map options) {
        globalBuildParameters << options
    }

    public boolean hasParameters() {
        return !globalBuildParameters.isEmpty()
    }

    public List getParameters() {
        return globalBuildParameters()
    }

    public static reset() {
        globalBuildParameters = []
        appliedOnce = false
    }
}
