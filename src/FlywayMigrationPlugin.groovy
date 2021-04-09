class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin {
    public static Map<String,String> outputMappings = [:]
    public static String password

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new FlywayMigrationPlugin())
    }

    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(TerraformEnvironmentStage.PLAN, flywayInfoClosure())
    }

    public Closure flywayInfoClosure() {
        return { innerClosure ->
            innerClosure()
            sh "echo run flyway info"
        }
    }

    public static convertOutputToEnvironmentVariable(String output, String variableName) {
        outputMappings[variableName] = output
        return this
    }

    public static withPassword(String password) {
        this.password = password
        return this
    }
}
