class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    DestroyPlugin() {
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
