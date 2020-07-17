class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        ConfirmApplyPlugin.set_command("destroy")
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
