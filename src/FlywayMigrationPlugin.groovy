class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    public static String passwordVariable
    public static String userVariable
    public static boolean echoEnabled = false

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
                sh buildFlywayCommand(command)
            }
        }
    }

    public Closure flywayMigrateClosure() {
        return { innerClosure ->
            innerClosure()

            def environmentVariables = buildEnvironmentVariableList(env)
            withEnv(environmentVariables) {
                def command = new FlywayCommand('migrate')
                sh buildFlywayCommand(command)
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

    public String buildFlywayCommand(FlywayCommand command) {
        def pieces = []
        if (!echoEnabled) {
            pieces << 'set +x'
        }
        pieces << command.toString()
        if (!echoEnabled) {
            pieces << 'set -x'
        }

        return pieces.join('\n')
    }

    public static withPasswordFromEnvironmentVariable(String passwordVariable) {
        this.passwordVariable = passwordVariable
        return this
    }

    public static withUserFromEnvironmentVariable(String userVariable) {
        this.userVariable = userVariable
        return this
    }

    public static withEchoEnabled(boolean trueOrFalse = true) {
        this.echoEnabled = trueOrFalse
        return this
    }

    public static reset() {
        passwordVariable = null
        userVariable = null
        echoEnabled = false
    }
}
