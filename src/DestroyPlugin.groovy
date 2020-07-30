class DestroyPlugin implements TerraformPlanCommandPlugin,
                               TerraformApplyCommandPlugin {

    private static arguments = []
    public static DESTROY_CONFIRM_MESSAGE = 'WARNING! Are you absolutely sure the plan above is correct? Your environment will be IMMEDIATELY DESTROYED via "terraform destroy"'
    public static DESTROY_OK_MESSAGE = "Run terraform DESTROY now"

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()

        ConfirmApplyPlugin.withConfirmMessage(DESTROY_CONFIRM_MESSAGE)
        ConfirmApplyPlugin.withOkMessage(DESTROY_OK_MESSAGE)
        TerraformEnvironmentStage.withStageNamePattern { options -> "${options['command']}-DESTROY-${options['environment']}" }

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
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
