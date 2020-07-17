class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
