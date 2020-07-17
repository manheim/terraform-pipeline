import static TerraformEnvironmentStage.CONFIRM

class ConfirmApplyPlugin implements TerraformEnvironmentStagePlugin {

    public static enabled = true
    public static command = "apply"

    ConfirmApplyPlugin() {
    }

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new ConfirmApplyPlugin())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        if (enabled) {
            stage.decorate(CONFIRM, addConfirmation(command)
        }
    }

    public static Closure addConfirmation(String command_name) {
        return { closure ->
            // ask for human input
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    input(
                        message: "Are you absolutely sure the plan above is correct, and should be IMMEDIATELY DEPLOYED via \"terraform ${command_name}\"?",
                        ok: "Run terraform ${command_name.toUpperCase()} now",
                        submitterParameter: 'approver'
                    )
                }
            } catch (ex) {
                throw ex
            }
            closure()
        }
    }

    public void set_command(String command) {
        this.command = command
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
