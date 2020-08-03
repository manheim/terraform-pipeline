import static TerraformEnvironmentStage.CONFIRM

class ConfirmApplyPlugin implements TerraformEnvironmentStagePlugin {

    public static parameters = []
    public static enabled = true
    public static String confirmMessage = 'Are you absolutely sure the plan above is correct, and should be IMMEDIATELY DEPLOYED via "terraform apply"?'
    public static String okMessage = 'Run terraform apply now'
    public static String submitter = 'approver'

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new ConfirmApplyPlugin())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        if (enabled) {
            stage.decorate(CONFIRM, addConfirmation(stage.getEnvironment()))
        }
    }

    public Closure addConfirmation(String environment) {
        return { closure ->
            // ask for human input
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    def inputOptions = getInputOptions(environment)
                    input(inputOptions)
                }
            } catch (ex) {
                throw ex
            }
            closure()
        }
    }

    private Map getInputOptions(String environment) {
        Map inputOptions = interpolateMap([
            message: confirmMessage,
            ok: okMessage,
            submitterParameter: submitter
        ], environment)

        if (!parameters.isEmpty()) {
            inputOptions['parameters'] = parameters.collect { item -> interpolateMap(item, environment) }
        }

        return inputOptions
    }

    public Map interpolateMap(Map input, String environment) {
        return input.inject([:]) { memo, key, value ->
            memo[key] = value.replaceAll('\\$\\{environment\\}', environment)
            memo
        }
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
