import static TerraformValidateStage.ALL
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.APPLY

class ParameterStoreBuildWrapperPlugin implements TerraformValidateStagePlugin, TerraformEnvironmentStagePlugin {
    private static globalPathPattern
    private static ArrayList globalParameterOptions = []
    private static defaultPathPattern = { options -> "/${options['organization']}/${options['repoName']}/${options['environment']}/" }

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new ParameterStoreBuildWrapperPlugin())
        TerraformValidateStage.addPlugin(new ParameterStoreBuildWrapperPlugin())
    }

    public static withPathPattern(Closure newPathPattern) {
        globalPathPattern = newPathPattern
        return this
    }

    public static withGlobalParameter(String path, options=[]) {
        globalParameterOptions << [path: path] + options
        return this
    }

    @Override
    public void apply(TerraformValidateStage stage) {
        globalParameterOptions.each { gp ->
            stage.decorate(PLAN, addParameterStoreBuildWrapper(gp))
            stage.decorate(APPLY, addParameterStoreBuildWrapper(gp))
        }
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        def environment = stage.getEnvironment()
        def parameterStorePath = pathForEnvironment(environment)

        def options = [
            path: parameterStorePath,
            credentialsId: "${environment.toUpperCase()}_PARAMETER_STORE_ACCESS"
        ]

        stage.decorate(PLAN, addParameterStoreBuildWrapper(options))
        stage.decorate(APPLY, addParameterStoreBuildWrapper(options))

        globalParameterOptions.each { gp ->
            stage.decorate(PLAN, addParameterStoreBuildWrapper(gp))
            stage.decorate(APPLY, addParameterStoreBuildWrapper(gp))
        }
    }

    String pathForEnvironment(String environment) {
        String organization = Jenkinsfile.instance.getOrganization()
        String repoName = Jenkinsfile.instance.getRepoName()
        def patternOptions = [ environment: environment,
                               repoName: repoName,
                               organization: organization ]

        def pathPattern = globalPathPattern ?: defaultPathPattern

        return pathPattern(patternOptions)
    }

    public static Closure addParameterStoreBuildWrapper(Map options = []) {
        def Map defaultOptions = [
            naming: 'basename'
        ]

        def parameterStoreOptions = defaultOptions + options

        return { closure ->
            // sh "echo ${parameterStoreOptions}" //DEBUG
            withAWSParameterStore(parameterStoreOptions) {
                closure()
            }
        }
    }

    public static reset() {
        globalPathPattern = null
        globalParameterOptions = []
    }
}
