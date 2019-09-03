import static TerraformEnvironmentStage.ALL

class DefaultEnvironmentPlugin implements TerraformEnvironmentStagePlugin {

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        String environment = stage.getEnvironment()

        stage.decorate(ALL, addEnvironmentTerraformVariable(environment))
    }

    public static Closure addEnvironmentTerraformVariable(String environment) {
        return { closure -> withEnv(["TF_VAR_environment=${environment}"]) { closure() } }
    }

}
