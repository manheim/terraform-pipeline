class TerraformOutputCommand implements TerraformCommand, Resettable {
    private static final DEFAULT_PLUGINS = []
    private String command = "output"
    private String terraformBinary = "terraform"
    String environment
    private String stateFilePath
    private static plugins = DEFAULT_PLUGINS.clone()
    private appliedPlugins = []

    public TerraformOutputCommand(String environment) {
        this.environment = environment
    }

    public String toString() {
        applyPluginsOnce()

        def pieces = []
        pieces << terraformBinary
        pieces << command

        return pieces.join(' ')
    }

    private applyPluginsOnce() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformOutputCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static addPlugin(TerraformOutputCommandPlugin plugin) {
        plugins << plugin
    }

    public static TerraformOutputCommand instanceFor(String environment) {
        return new TerraformOutputCommand(environment)
    }

    public static getPlugins() {
        return plugins
    }

    public static reset() {
        this.plugins = DEFAULT_PLUGINS.clone()
    }

    public String getEnvironment() {
        return environment
    }
}