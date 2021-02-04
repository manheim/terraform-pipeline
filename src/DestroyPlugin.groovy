class DestroyPlugin implements TerraformPlanCommandPlugin,
                               TerraformApplyCommandPlugin,
                               Resettable {

    private static arguments = []
    public static DESTROY_CONFIRM_MESSAGE = 'DANGER! Your ${environment} environment will be IMMEDIATELY DESTROYED via "terraform destroy".  Review your plan to see all the resources that will be destroyed, and confirm. YOU CANNOT UNDO THIS.'
    public static DESTROY_OK_MESSAGE = "Run terraform DESTROY now"

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()

        def appName = Jenkinsfile.instance.getRepoName()

        ConfirmApplyPlugin.withConfirmMessage(DESTROY_CONFIRM_MESSAGE)
        ConfirmApplyPlugin.withOkMessage(DESTROY_OK_MESSAGE)
        ConfirmApplyPlugin.withParameter([
                $class: 'hudson.model.StringParameterDefinition',
                name: 'CONFIRM_DESTROY',
                description: "Type \"destroy ${appName} \${environment}\" to confirm and continue."
            ])
        ConfirmApplyPlugin.withConfirmCondition(getConfirmCondition(appName))

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

    public static getConfirmCondition(String appName) {
        return { options ->
            "destroy ${appName} ${options['environment']}".toString() == options['input']['CONFIRM_DESTROY']
        }
    }

    public static reset() {
        arguments = []
    }
}
