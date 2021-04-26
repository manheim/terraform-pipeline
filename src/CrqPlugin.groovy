class CrqPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new CrqPlugin())
    }

    CrqPlugin() {
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        def environment = stage.getEnvironment()

        stage.decorate(TerraformEnvironmentStage.APPLY, addCrq(environment))
    }

    public Closure addCrq(String environment) {
        def env = getEnv()

        return { closure ->

            // Only Open CRQ's for prod/uat environments
            if (environment == "prod" or environment == "uat") {
                def config = [
                    component:         env['CRQ_COMPONENT'],
                    component_id:      env['CRQ_COMPONENT_ID'],
                    short_description: env['CRQ_SHORT_DESCRIPTION'],
                    description:       env['CRQ_DESCRIPTION'],
                    notes:             env['CRQ_NOTES']       ?: '',
                    work_notes:        env['CRQ_WORK_NOTES']  ?: '',
                    sandbox:           env['CRQ_SANDBOX']     ?: 'false',
                    auth_method:       env['CRQ_AUTH_METHOD'] ?: 'machine_auth'
                ]

                // OpenCRQ
                sh "${crqOpen(config)}"

                // Add Work Notes
                for (note in config.work_notes.split(',')) {
                    sh "${crqAddWorkNote(note, config)}"
                }

                // Close or BackoutCRQ
                try {
                    closure()
                    sh "${crqClose(config)}"
                } catch (err) {
                    sh "${crqBackout(config)}"
                    throw err
                }
            } else {
                sh "echo environment not 'prod' or 'uat', set this to trigger automated CRQs"
                closure()
            }
        }
    }

    public static String crqOpen(config = [:]) {
        // The component can be specified by name or ID. If both are specified, then only the ID will be used.
        if (config.component_id):
            return "config.crqNumber = openCrq(component_id: \"${config.component_id}\", short_description: \"${config.short_description}\", description: \"${config.description}\", sandbox: \"${config.sandbox}\", auth_method: \"${config.auth_method}\")"
        else:
            return "config.crqNumber = openCrq(component: \"${config.component}\", short_description: \"${config.short_description}\", description: \"${config.description}\", sandbox: \"${config.sandbox}\", auth_method: \"${config.auth_method}\")"

    }

    public static String crqClose(config = [:]) {
        return "closeCrq(number: \"config.crqNumber\", notes: \"Change Successful\", sandbox: \"${config.sandbox}\", auth_method: \"${config.auth_method}\")"
    }

    public static String crqBackout(config = [:]) {
        return "closeCrq(number: \"config.crqNumber\", notes: \"Change Failed\", state: \"Unsuccessful\", sandbox: \"${config.sandbox}\", auth_method: \"${config.auth_method}\")"
    }

    public static String crqAddWorkNote(note, config = [:]) {
        return "addWorkNoteCrq(number: \"config.crqNumber\", work_note: \"${note}\", sandbox: \"${config.sandbox}\", auth_method: \"${config.auth_method}\")"
    }

    public getEnv() {
        return (Jenkinsfile.instance != null) ? Jenkinsfile.instance.getEnv() : [:]
    }

    public String getRepoName() {
        return (Jenkinsfile.instance != null) ? Jenkinsfile.instance.getRepoName() : null
    }
}
