/**
 * Development Notes For Support version-specific workflows
 *
 * 1. Create a new class that extends TerraformPluginVersion
 * 2. Implement `apply` methods for any TerraformCommand and TerraformStage necessary
 * 3. Modify `strategyFor`, to return your new TerraformPluginVersion, for which
 *    ever set of terraform-versions that require your new workflow changes.
 *
 * See TerraformPluginVersion11 or TerraformPluginVersion12 for an example
 * before strating on your own.
 */
class TerraformPlugin implements TerraformValidateCommandPlugin, TerraformValidateStagePlugin {

    static String version
    static final String DEFAULT_VERSION = '0.11.0'
    public static TERRAFORM_VERSION_FILE = '.terraform-version'

    public static init() {
        def plugin = new TerraformPlugin()

        TerraformValidateCommand.addPlugin(plugin)
        TerraformValidateStage.addPlugin(plugin)
    }

    public String detectVersion() {
        if (version == null) {
            if (fileExists(TERRAFORM_VERSION_FILE)) {
                version = readFile(TERRAFORM_VERSION_FILE)
            } else {
                version = DEFAULT_VERSION
            }
        }

        return version
    }

    public static String checkVersion() {
        return Jenkinsfile.build(pipelineConfiguration())
    }

    private static Closure pipelineConfiguration() {
        def closure = {
            node {
                deleteDir()
                checkout(scm)
                def plugin = new TerraformPlugin()

                return plugin.detectVersion()
            }
        }

        closure.resolveStrategy = Closure.DELEGATE_ONLY
        return closure
    }

    public TerraformPluginVersion strategyFor(String version) {
        // if (new SemanticVersion(version) >= new SemanticVersion('0.12.0')) should be used
        // here.  Unit tests pass with the above, but running Jenkinsfile in a pipeline context
        // does not.  Debug statements show that the above will return 0 when it should return 'true'.
        if ((new SemanticVersion(version) <=> new SemanticVersion('0.12.0')) >= 0) {
            return new TerraformPluginVersion12()
        }

        return new TerraformPluginVersion11()
    }

    static void withVersion(String userVersion) {
        this.version = userVersion
    }

    static  void resetVersion() {
        this.version = null
    }

    public boolean fileExists(String filename) {
        return getJenkinsOriginal().fileExists(filename)
    }

    public String readFile(String filename) {
        def content = (getJenkinsOriginal().readFile(filename) as String)
        return content.trim()
    }

    public getJenkinsOriginal() {
        return  Jenkinsfile.instance.original
    }

    public String test(String test){
        def content = "fdasfds "
        return content.trim()
    }

    @Override
    void apply(TerraformValidateCommand command) {
        def version = detectVersion()

        def strategy = strategyFor(version)
        strategy.apply(command)
    }

    @Override
    void apply(TerraformValidateStage validateStage) {
        validateStage.decorate(TerraformValidateStage.ALL, modifyValidateStage(validateStage))
    }

    public Closure modifyValidateStage(validateStage) {
        return { closure ->
            def version = detectVersion()

            def strategy = strategyFor(version)
            strategy.apply(validateStage)

            closure()
        }
    }
}
