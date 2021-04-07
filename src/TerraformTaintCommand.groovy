class TerraformTaintCommand implements TerraformCommand, Resettable {
    private String command = "taint"
    private String resource
    private String environment
    private static plugins = []
    private appliedPlugins = []

    public TerraformTaintCommand(String environment) {
        this.environment = environment
    }

    public TerraformTaintCommand withResource(String resource) {
        this.resource = resource
        return this
    }

    public String toString() {
        applyPlugins()
        def parts = []
        parts << 'terraform'
        parts << command
        parts << resource

        parts.removeAll { it == null }
        return parts.join(' ')
    }

    public String getResource() {
        return this.resource
    }

    public static TerraformTaintCommand instanceFor(String environment) {
        return new TerraformTaintCommand(environment)
    }

    public String getEnvironment() {
        return this.environment
    }

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformTaintCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformTaintCommandPlugin plugin) {
        plugins << plugin
    }

    public static void setPlugins(plugins) {
        this.plugins = plugins
    }

    public static getPlugins() {
        return plugins
    }

    public static void reset() {
        this.plugins = []
    }
}
