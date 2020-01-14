/**
 * Development Notes
 *
 * 1. Add the correct plugin interfaces (e.g. `TerraformValidateCommandPlugin`).
 * 2. Add the correct method if not arleady present.  If you do, ensure that you
 *    call `detectVersion()` at the beginning of the method.
 * 3. Introduce logic to the method that utilizes the `Comparable` class,
 *   `SemanticVersion` to determine what behavior.  The original break with
 *   the removal of the `-check-variables=false` in the
 *   `apply(TerraformValidateCommand)` provides a working example.
 * 4. If you introduced a new plugin interface, ensure that you add the
 *   the plugin class to the appropriate stage or command's `DEFAULT_PLUGINS`
 *   list.  See `TerraformValidateCommand` for an example.
 * 5. Finally, update the tests in `TerraformPluginTests` to cover the new
 *   behavior.
 *
 * See apply(TerraformValidateCommand) for an over-documented example
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
        if(version == null) {
            if (fileExists(TERRAFORM_VERSION_FILE)) {
                version = new SemanticVersion(readFile(TERRAFORM_VERSION_FILE))
            } else {
                version = new SemanticVersion(DEFAULT_VERSION)
            }
        }

        return version
    }

    public TerraformPluginVersion strategyFor(String version) {
        if(new SemanticVersion(version).compareTo(new SemanticVersion('0.12.0')) >= 0) {
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
        // This must be at the top of every apply() call.  This is
        // the best way to ensure that Jenkinsfile.instance.original is
        // properly set so that we can detect the terraform-version.
        def version = detectVersion()

        // Due to CPS shenanigans you can't use Groovy magic and do
        // version < new SemanticVersion('1.2.3').  This becomes
        // if(-1) or if(0) or if(1) resulting in truthy values for
        // both less than and greater than and false-ey values when
        // equal.

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
