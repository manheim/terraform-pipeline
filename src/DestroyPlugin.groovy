class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()

        ConfirmApplyPlugin.withConfirmationMessage("YOU ARE ABOUT TO DESTROY YOUR ENVIRONMENT")
        ConfirmApplyPlugin.withOkMessage("!!!DESTROY DESTROY DESTROY!!!")
        TerraformPlanCommand.withArgument('-destroy')
        TerraformApplyCommand.withCommand('destroy')


        //TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
    }

}
