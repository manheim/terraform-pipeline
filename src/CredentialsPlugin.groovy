class CredentialsPlugin implements BuildStagePlugin, RegressionStagePlugin, TerraformEnvironmentStagePlugin, TerraformValidateStagePlugin, Resettable {
    private static bindings = []

    public static void init() {
        def plugin = new CredentialsPlugin()

        BuildStage.addPlugin(plugin)
        RegressionStage.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformValidateStage.addPlugin(plugin)
    }

    public static withBinding(Closure binding) {
        bindings << binding
        return this
    }

    // Deprecated: Remove this with Issue #404 and the next major release
    public static withBuildCredentials(Map options = [:], String credentialsId) {
        Map optionsWithDefaults = populateDefaults(options, credentialsId)
        bindings << { usernamePassword(optionsWithDefaults) }
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
            def appliedBindings = bindings.collect { it -> it() }

            withCredentials(appliedBindings, innerClosure)
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

    public static getBindings() {
        return bindings
    }

    public static void reset() {
        bindings = []
    }

}
