class DestroyPlugin implements TerraformEnvironmentStagePlugin,
                               TerraformPlanCommandPlugin,
                               TerraformApplyCommandPlugin {

    private static arguments = []

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()

        ConfirmApplyPlugin.withConfirmMessage('WARNING! Are you absolutely sure the plan above is correct? Your environment will be IMMEDIATELY DESTROYED via "terraform destroy"')
        ConfirmApplyPlugin.withOkMessage("Run terraform DESTROY now")

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        // Change stage name to append the word 'destroy' so it's clear that it's altered
        // the Stage
    }

    public void apply(TerraformPlanCommand command) {
        command.withArgument('-destroy')
    }

    public void apply(TerraformApplyCommand command) {
        command.withCommand('destroy')
        for (arg in arguments) {
            command.withArgument(arg)
        }
    }

    public static withArgument(String arg) {
        arguments << arg
        return this
    }

    public static reset() {
        arguments = []
    }
}
