class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    public static Map<String,String> outputMappings = [:]
    public static String passwordVariable
    public static String userVariable

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

            if (passwordVariable) {
                environmentVariables << "FLYWAY_PASSWORD=${env[passwordVariable]}"
            }

            if (userVariable) {
                environmentVariables << "FLYWAY_USER=${env[userVariable]}"
            }

            withEnv(environmentVariables) {
                def command = new FlywayCommand('info')
                sh command.toString()
            }
        }
    }

    public static convertOutputToEnvironmentVariable(String output, String variableName) {
        outputMappings[variableName] = output
        return this
    }

    public static withPasswordFromEnvironmentVariable(String passwordVariable) {
        this.passwordVariable = passwordVariable
        return this
    }

    public static withUserFromEnvironmentVariable(String userVariable) {
        this.userVariable = userVariable
        return this
    }

    public static reset() {
        passwordVariable = null
        userVariable = null
    }
}
