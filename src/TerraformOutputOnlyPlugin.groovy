import static TerraformEnvironmentStage.PLAN_COMMAND
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.CONFIRM

class TerraformOutputOnlyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        TerraformOutputOnlyPlugin plugin = new TerraformOutputOnlyPlugin()

        BuildWithParametersPlugin.withBooleanParameter([
            name: "SHOW_OUTPUTS_ONLY",
            description: "Only run 'terraform output' to show outputs, skipping plan and apply"
        ])

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public Closure skipStage(String stageName) {
        return { closure ->
            echo "Skipping ${stageName} stage. TerraformOutputOnlyPlugin is enabled."
        }
    }

    public Closure runTerraformOutputCommand(String environment) {
        outputCommand = TerraformOutputCommand.instanceFor(environment)
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
}