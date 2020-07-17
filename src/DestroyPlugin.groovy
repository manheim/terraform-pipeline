class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()
        
        // Need to set command name to "destroy"
        ConfirmApplyPlugin.withCommand("destroy")
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
