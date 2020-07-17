class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    @Override
    public apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
