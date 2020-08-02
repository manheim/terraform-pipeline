import static TerraformEnvironmentStage.CONFIRM

class ConfirmApplyPlugin implements TerraformEnvironmentStagePlugin {

    public static parameters = []
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

    public Closure addConfirmation() {
        return { closure ->
            // ask for human input
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    input(getInputOptions())
                }
            } catch (ex) {
                throw ex
            }
            closure()
        }
    }

    private Map getInputOptions() {
        Map inputOptions = [
            message: confirmMessage,
            ok: okMessage,
            submitterParameter: submitter
        ]

        if (!parameters.isEmpty()) {
            inputOptions['parameters'] = parameters
        }

        return inputOptions
    }

    public static void withParameter(Map parameterOptions) {
        parameters << parameterOptions
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

    public static reset() {
        this.enabled = true
        this.parameters = []
    }
}
