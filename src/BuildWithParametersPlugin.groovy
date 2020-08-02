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
        println "do the thing"
    }

    @Override
    public void apply(TerraformValidateStage stage) {
        println "do the thing"
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        println "do the thing"
    }

    @Override
    public void apply(RegressionStage stage) {
        println "do the thing"
    }
}
