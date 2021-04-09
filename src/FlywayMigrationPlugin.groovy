class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    public static Map<String,String> outputMappings = [:]
    public static String password
    public static String user

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

    public static withUser(String user) {
        this.user = user
        return this
    }

    public static getEnvironmentVariableList() {
        def result = []

        if (this.password) {
            result << "FLYWAY_PASSWORD=${password}"
        }

        return result
    }

    public static reset() {
        password = null
    }
}
