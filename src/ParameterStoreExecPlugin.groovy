import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.DESTROY

class ParameterStoreExecPlugin implements TerraformEnvironmentStagePlugin, TerraformPlanCommandPlugin, TerraformApplyCommandPlugin {
    public static void init() {
        ParameterStoreExecPlugin plugin = new ParameterStoreExecPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        def environment = stage.getEnvironment()
        def parameterStorePath = pathForEnvironment(environment)

        stage.decorate(PLAN, addEnvVariables(parameterStorePath))
        stage.decorate(APPLY, addEnvVariables(parameterStorePath))
        stage.decorate(DESTROY, addEnvVariables(parameterStorePath))
    }

    public String pathForEnvironment(String environment) {
        String organization = Jenkinsfile.instance.getOrganization()
        String repoName = Jenkinsfile.instance.getRepoName()

        return "/${organization}/${repoName}/${environment}/"
    }

    public static Closure addEnvVariables(String path) {
        return { closure ->
            withEnv(["PARAMETER_STORE_EXEC_PATH=${path}", "PARAMETER_STORE_EXEC_DISABLE_TRANSLATION=true", "AWS_REGION=us-east-1"]) {
                closure()
            }
        }
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withPrefix('parameter-store-exec')
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        command.withPrefix('parameter-store-exec')
    }
}
