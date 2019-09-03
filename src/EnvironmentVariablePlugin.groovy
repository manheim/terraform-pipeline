import static TerraformEnvironmentStage.ALL

class EnvironmentVariablePlugin implements TerraformEnvironmentStagePlugin {
    private String key
    private String value

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(ALL, withEnvClosure(key, value))
    }

    public withEnv(String key, String value) {
        this.key = key
        this.value = value
    }

    private Closure withEnvClosure(String key, String value) {
        return { closure ->
            withEnv(["${key}=${value}"]) {
                closure()
            }
        }
    }

}
