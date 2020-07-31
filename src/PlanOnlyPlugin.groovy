import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.CONFIRM

class PlanOnlyPlugin implements TerraformEnvironmentStagePlugin, TerraformPlanCommandPlugin {

    public static void init() {
        PlanOnlyPlugin plugin = new PlanOnlyPlugin()

        Jenkinsfile.instance.addParam([
            $class: 'hudson.model.BooleanParameterDefinition',
            name: 'FAIL_PLAN_ON_CHANGES',
            defaultValue: false,
            description: 'Plan run with -detailed-exitcode; ANY CHANGES will cause failure'
        ])

        TerraformPlanCommand.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public Closure skipStage(String stageName) {
        return  { closure ->
            echo "Skipping ${stageName} stage. PlanOnlyPlugin is enabled."
        }
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorateAround(CONFIRM, skipStage(CONFIRM))
        stage.decorateAround(APPLY, skipStage(APPLY))
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        if (Jenkinsfile.instance.getEnv().FAIL_PLAN_ON_CHANGES == 'true') {
            command.withPrefix('set -e; set -o pipefail;')
            command.withArgument('-detailed-exitcode')
        }
    }
}
