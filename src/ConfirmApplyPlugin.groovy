import static TerraformEnvironmentStage.CONFIRM

class ConfirmApplyPlugin implements TerraformEnvironmentStagePlugin {

    public static enabled = true
    public static String confirmMessage = 'Are you absolutely sure the plan above is correct, and should be IMMEDIATELY DEPLOYED via "terraform apply"?'
    public static String okMessage = 'Run terraform apply now'
    public static String submitter = 'approver'

    ConfirmApplyPlugin() {
    }

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new ConfirmApplyPlugin())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        if (enabled) {
            stage.decorate(CONFIRM, addConfirmation())
        }
    }

    public static Closure addConfirmation() {
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

    //public static String getConfirmMessage() {
    //    return this.confirmMessage
   // }

    //public static String getOkMessage() {
    //    return this.okMessage
   // }

    //public static String getSubmitter() {
    //    return this.submitter
    //}

    public static disable() {
        this.enabled = false
        return this
    }

    public static enable() {
        this.enabled = true
        return this
    }
}
