class DeployAndDestroyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        DeployAndDestroyPlugin plugin = new DeployAndDestroyPlugin()

        ConfirmApplyPlugin.withConfirmMessage('WARNING! Are you absolutely sure the plan above is correct? Your environment will be IMMEDIATELY DESTROYED via "terraform destroy"')
        ConfirmApplyPlugin.withOkMessage("Run terraform DESTROY now")

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DeployAndDestroyStrategy())
    }

}
