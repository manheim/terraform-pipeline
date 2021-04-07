class TerraformUntaintCommand implements TerraformCommand, Resettable {
    private String command = "untaint"
    private String resource
    private String environment
    private static plugins = []
    private appliedPlugins = []

    public TerraformUntaintCommand(String environment) {
        this.environment = environment
    }

    public TerraformUntaintCommand withResource(String resource) {
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

    public static TerraformUntaintCommand instanceFor(String environment) {
        return new TerraformUntaintCommand(environment)
    }

    public String getEnvironment() {
        return this.environment
    }

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformUntaintCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformUntaintCommandPlugin plugin) {
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

