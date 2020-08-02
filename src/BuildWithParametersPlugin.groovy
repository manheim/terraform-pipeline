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
                    parameters(getBuildParameters())
                ])
                appliedOnce = true
            }

            innerClosure()
        }
    }

    public static withBooleanParameter(Map options) {
        def optionDefaults = [
            defaultValue: false,
            $class: 'hudson.model.BooleanParameterDefinition'
        ]

        globalBuildParameters << (optionDefaults + options)
    }

    public static withStringParameter(Map options) {
        def optionDefaults = [
            defaultValue: '',
            $class: 'hudson.model.StringParameterDefinition'
        ]

        globalBuildParameters << (optionDefaults + options)
    }

    public static withParameter(Map options) {
        globalBuildParameters << options
    }

    public boolean hasParameters() {
        return !globalBuildParameters.isEmpty()
    }

    public List getBuildParameters() {
        return globalBuildParameters
    }

    public static reset() {
        globalBuildParameters = []
        appliedOnce = false
    }
}
