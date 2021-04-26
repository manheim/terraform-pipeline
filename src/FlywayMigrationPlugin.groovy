class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    public static Map<String,String> variableMap = [:]
    public static boolean echoEnabled = false
    public static boolean confirmBeforeApply = true

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

    public boolean hasPendingMigration(workflowScript) {
        def closure = {
            def resultString = sh (
                script: 'set +e; grep Pending flyway_output.txt > /dev/null; if [ $? -eq 0 ]; then echo true; else echo false; fi',
                returnStdout: true
            ).trim()
            return new Boolean(resultString)
        }

        closure.delegate = workflowScript
        return closure()
    }

    public void confirmMigration(workflowScript) {
        def closure = {
            timeout(time: 1, unit: 'MINUTES') {
                input("One or more pending migrations will be applied immediately if you continue - please review the flyway info output.  Are you sure you want to continue?")
            }
        }

        closure.delegate = workflowScript
        closure()
    }

    public Closure flywayMigrateClosure() {
        return { innerClosure ->
            if (confirmBeforeApply && hasPendingMigration(delegate)) {
                confirmMigration(delegate)
            }

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

        if (confirmBeforeApply) {
            pieces << 'set -o pipefail'
        }

        def commandString = command.toString()
        if (confirmBeforeApply) {
            commandString += "| tee flyway_output.txt"
        }

        pieces << commandString

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

    public static confirmBeforeApplyingMigration(boolean trueOrFalse = true) {
        this.confirmBeforeApply = trueOrFalse
        return this
    }

    public static reset() {
        variableMap = [:]
        echoEnabled = false
        confirmBeforeApply = true
    }
}
