class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    public static String passwordVariable
    public static String userVariable

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new FlywayMigrationPlugin())
    }

    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(TerraformEnvironmentStage.PLAN, flywayInfoClosure())
        stage.decorate(TerraformEnvironmentStage.APPLY, flywayMigrateClosure())
    }

    public Closure flywayInfoClosure() {
        return { innerClosure ->
            innerClosure()

            def environmentVariables = buildEnvironmentVariableList(env)
            withEnv(environmentVariables) {
                def command = new FlywayCommand('info')
                sh command.toString()
            }
        }
    }

    public Closure flywayMigrateClosure() {
        return { innerClosure ->
            innerClosure()

            def environmentVariables = buildEnvironmentVariableList(env)
            withEnv(environmentVariables) {
                def command = new FlywayCommand('migrate')
                sh command.toString()
            }
        }
    }

    public Collection buildEnvironmentVariableList(env) {
        def list = []
        if (passwordVariable) {
            list << "FLYWAY_PASSWORD=${env[passwordVariable]}"
        }

        if (userVariable) {
            list << "FLYWAY_USER=${env[userVariable]}"
        }

        return list
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
