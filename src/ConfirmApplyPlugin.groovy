import static TerraformEnvironmentStage.CONFIRM

class ConfirmApplyPlugin implements TerraformEnvironmentStagePlugin {

    public static enabled = true

    ConfirmApplyPlugin() {
    }

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new ConfirmApplyPlugin())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        command_name = "apply"
        if (stage instanceof TerraformDestroyStage){
            command_name = "destroy"
        }

        if (enabled) {
            stage.decorate(CONFIRM, addConfirmation(command_name))
        }
    }

    public static Closure addConfirmation(String command) {
        return { closure ->
            // ask for human input
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    input(
                        message: "Are you absolutely sure the plan above is correct, and should be IMMEDIATELY DEPLOYED via \"terraform ${command}\"?",
                        ok: "Run terraform ${command.toUpperCase()} now",
                        submitterParameter: 'approver'
                    )
                }
            } catch (ex) {
                throw ex
            }
            closure()
        }
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
