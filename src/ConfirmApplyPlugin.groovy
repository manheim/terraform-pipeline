import static TerraformEnvironmentStage.CONFIRM

class ConfirmApplyPlugin implements TerraformEnvironmentStagePlugin {

    public static enabled = true
    private static String confirmMessage = 'Are you absolutely sure the plan above is correct, and should be IMMEDIATELY DEPLOYED via "terraform apply"?'
    private static String okMessage = 'Run terraform apply now'
    private static String submitter = 'approver'

    ConfirmApplyPlugin() {
    }

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new ConfirmApplyPlugin())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        if (enabled) {
            stage.decorate(CONFIRM, addConfirmation(command))
        }
    }

    public static Closure addConfirmation(String command_name) {
        return { closure ->
            // ask for human input
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    input(
                        message: confirmMessage,
                        ok: okMessage,
                        submitterParameter: submitter
                    )
                }
            } catch (ex) {
                throw ex
            }
            closure()
        }
    }

    public static void withConfirmMessage(String newMessage) {
        this.confirmMessage = newMessage
    }

    public static void withOkMessage(String newMessage) {
        this.okMessage = newMessage
    }

    public static void withSubmitterParameter(String newParam) {
        this.submitterParameter = newParam
    }

    public static disable() {
        this.enabled = false
        return this
    }

    public static enable() {
        this.enabled = true
        return this
    }
}
