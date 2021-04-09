class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    public static Map<String,String> outputMappings = [:]
    public static String passwordVariable
    public static String userVariable
    public static String locations

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

            def variableName = "TF_VAR_USERNAME"
            withEnv(environmentVariables) {
                def flywayCommand = []
                flywayCommand << "flyway"
                if (locations) {
                    flywayCommand << "-locations=\"${locations}\""
                }
                flywayCommand << "info"
                sh flywayCommand.join(' ')
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

    public static withLocation(String locations) {
        this.locations = locations
        return this
    }

    public static reset() {
        passwordVariable = null
        userVariable = null
        locations = null
    }
}
