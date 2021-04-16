class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    public static Map<String,String> variableMap = [:]
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
        variableMap.inject([]) { list, key, value ->
            list << "${key}=${env[value]}"
            list
        }
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

    public static withEchoEnabled(boolean trueOrFalse = true) {
        this.echoEnabled = trueOrFalse
        return this
    }

    public static withMappedEnvironmentVariable(String from, String to) {
        variableMap[to] = from
        return this
    }

    public static reset() {
        variableMap = [:]
        echoEnabled = false
    }
}
