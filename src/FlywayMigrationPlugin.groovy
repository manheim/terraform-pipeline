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
            def environmentVariables = outputMappings.collect { variable, outputId ->
                def outputValue = sh(
                    script: "terraform output ${outputId}",
                    returnStdout: true
                ).trim()
                "${variable}=${outputValue}"
            }

            if (password) {
                environmentVariables << "FLYWAY_PASSWORD=${password}"
            }

            if (user) {
                environmentVariables << "FLYWAY_USER=${user}"
            }

            withEnv(environmentVariables) {
                sh "echo run flyway info, user=\$FLYWAY_USER, url=\$FLYWAY_URL"
            }
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

        if (this.user) {
            result << "FLYWAY_USER=${user}"
        }

        return result
    }

    public static reset() {
        password = null
        user = null
    }
}
