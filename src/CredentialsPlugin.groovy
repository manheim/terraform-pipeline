class CredentialsPlugin implements BuildStagePlugin, RegressionStagePlugin, TerraformEnvironmentStagePlugin, TerraformValidateStagePlugin, Resettable {
    private static globalBuildCredentials = []

    public static void init() {
        def plugin = new CredentialsPlugin()

        BuildStage.addPlugin(plugin)
        RegressionStage.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformValidateStage.addPlugin(plugin)
    }

    public static withBuildCredentials(Map options = [:], String credentialsId) {
        Map optionsWithDefaults = populateDefaults(options, credentialsId)
        globalBuildCredentials << { usernamePassword(optionsWithDefaults) }
        return this
    }

    @Override
    public void apply(BuildStage buildStage) {
        buildStage.decorate(addBuildCredentials())
    }

    @Override
    public void apply(RegressionStage regressionStage) {
        regressionStage.decorate(addBuildCredentials())
    }

    @Override
    public void apply(TerraformEnvironmentStage environmentStage) {
        environmentStage.decorate(addBuildCredentials())
    }

    @Override
    public void apply(TerraformValidateStage validateStage) {
        validateStage.decorate(addBuildCredentials())
    }

    private addBuildCredentials() {
        return { innerClosure ->
            def credentials = globalBuildCredentials.collect { it -> it() }

            withCredentials(credentials, innerClosure)
        }
    }

    public static Map populateDefaults(Map options = [:], String credentialsId)  {
        def credentialsOptions = options.clone()
        credentialsOptions['credentialsId'] = credentialsId
        credentialsOptions['usernameVariable'] = credentialsOptions['usernameVariable'] ?: "${toEnvironmentVariable(credentialsId)}_USERNAME".toString()
        credentialsOptions['passwordVariable'] = credentialsOptions['passwordVariable'] ?: "${toEnvironmentVariable(credentialsId)}_PASSWORD".toString()

        return credentialsOptions
    }

    public static String toEnvironmentVariable(String value) {
        value.toUpperCase().replaceAll('-', '_')
    }

    public static getBuildCredentials() {
        return globalBuildCredentials
    }

    public static void reset() {
        globalBuildCredentials = []
    }

}
