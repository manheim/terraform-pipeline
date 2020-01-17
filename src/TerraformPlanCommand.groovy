class TerraformPlanCommand {
    private static final DEFAULT_PLUGINS = []
    private boolean input = false
    private String terraformBinary = "terraform"
    private String command = "plan"
    String environment
    private prefixes = []
    private arguments = []
    private static plugins = DEFAULT_PLUGINS.clone()
    private appliedPlugins = []
    private String directory

    public TerraformPlanCommand(String environment) {
        this.environment = environment
    }

    public TerraformPlanCommand withInput(boolean input) {
        this.input = input
        return this
    }

    public TerraformPlanCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformPlanCommand withDirectory(String directory) {
        this.directory = directory
        return this
    }

    public TerraformPlanCommand withArgument(String argument) {
        this.arguments << argument
        return this
    }

    public String toString() {
        applyPluginsOnce()

        def pieces = []
        pieces = pieces + prefixes
        pieces << terraformBinary
        pieces << command
        if (!input) {
            pieces << "-input=false"
        }
        pieces += arguments
        if (directory) {
            pieces << directory
        }

        return pieces.join(' ')
    }

    private applyPluginsOnce() {
        def remainingPlugins = plugins - appliedPlugins

        for(TerraformPlanCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static addPlugin(TerraformPlanCommandPlugin plugin) {
        plugins << plugin
    }

    public static TerraformPlanCommand instanceFor(String environment) {
        return new TerraformPlanCommand(environment)
            .withInput(false)
    }

    public static getPlugins() {
        return plugins
    }

    public static resetPlugins() {
        this.plugins = DEFAULT_PLUGINS.clone()
    }
}
