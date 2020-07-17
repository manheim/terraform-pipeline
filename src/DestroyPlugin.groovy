class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    DestroyPlugin() {
    }

    public static void init() {}

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
