import static TerraformEnvironmentStage.INIT_COMMAND
import static TerraformEnvironmentStage.PLAN_COMMAND
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.CONFIRM

class TerraformOutputOnlyPlugin implements TerraformEnvironmentStagePlugin, TerraformOutputCommandPlugin {

    public static void init() {
        TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()

        BuildWithParametersPlugin.withBooleanParameter([
            name: "SHOW_OUTPUTS_ONLY",
            description: "Only run 'terraform output' to show outputs, skipping plan and apply."
        ])
        BuildWithParametersPlugin.withBooleanParameter([
            name: "JSON_FORMAT_OUTPUTS",
            description: "Render 'terraform output' results as JSON. Only applies if SHOW_OUTPUTS_ONLY is selected."
        ])
        BuildWithParametersPlugin.withStringParameter([
            name: "REDIRECT_OUTPUTS_TO_FILE",
            description: "Filename relative to the current workspace to redirect output to. Only applies if 'SHOW_OUTPUTS_ONLY' is selected."
        ])

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformOutputCommand.addPlugin(plugin)
    }

    public Closure skipStage(String stageName) {
        return { closure ->
            echo "Skipping ${stageName} stage. TerraformOutputOnlyPlugin is enabled."
        }
    }

    public Closure runTerraformOutputCommand(String environment) {
        def outputCommand = TerraformOutputCommand.instanceFor(environment)
        return { closure ->
            closure()
            echo "Running 'terraform output'. TerraformOutputOnlyPlugin is enabled."
            sh outputCommand.toString()
        }
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        if (Jenkinsfile.instance.getEnv().SHOW_OUTPUTS_ONLY == 'true') {
            stage.decorate(INIT_COMMAND, runTerraformOutputCommand(stage.getEnvironment()))
            stage.decorate(PLAN_COMMAND, skipStage(PLAN_COMMAND))
            stage.decorateAround(CONFIRM, skipStage(CONFIRM))
            stage.decorateAround(APPLY, skipStage(APPLY))
        }
    }

    @Override
    public void apply(TerraformOutputCommand command) {
        if (Jenkinsfile.instance.getEnv().JSON_FORMAT_OUTPUTS == 'true') {
            command.withJson(true)
        }
        if (Jenkinsfile.instance.getEnv().REDIRECT_OUTPUTS_TO_FILE) {
            command.withRedirectFile(Jenkinsfile.instance.getEnv().REDIRECT_OUTPUTS_TO_FILE)
        }
    }
}