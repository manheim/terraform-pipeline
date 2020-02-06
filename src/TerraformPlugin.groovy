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

    static SemanticVersion version
    static final String DEFAULT_VERSION = '0.11.0'
    public static TERRAFORM_VERSION_FILE = '.terraform-version'

    public static init() {
        def plugin = new TerraformPlugin()

        TerraformValidateCommand.addPlugin(plugin)
        TerraformValidateStage.addPlugin(plugin)
    }

    public SemanticVersion detectVersion() {
        if (version == null) {
            if (fileExists(TERRAFORM_VERSION_FILE)) {
                version = new SemanticVersion(readFile(TERRAFORM_VERSION_FILE))
            } else {
                version = new SemanticVersion(DEFAULT_VERSION)
            }
        }

        return version
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

    static void withVersion(String version) {
        this.version = new SemanticVersion(version)
    }

    static  void resetVersion() {
        this.version = null
    }

    public boolean fileExists(String filename) {
        return Jenkinsfile.instance.original.fileExists(filename)
    }

    public String readFile(String filename) {
        return (Jenkinsfile.instance.original.readFile(TERRAFORM_VERSION_FILE) as String).trim()
    }

    @Override
    void apply(TerraformValidateCommand command) {
        def version = detectVersion()

        def strategy = strategyFor(version.version)
        strategy.apply(command)
    }

    @Override
    void apply(TerraformValidateStage validateStage) {
        validateStage.decorate(TerraformValidateStage.ALL, modifyValidateStage(validateStage))
    }

    public Closure modifyValidateStage(validateStage) {
        return { closure ->
            def version = detectVersion()

            def strategy = strategyFor(version.version)
            strategy.apply(validateStage)

            closure()
        }
    }
}
