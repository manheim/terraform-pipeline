class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
