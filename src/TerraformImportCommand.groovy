class TerraformImportCommand implements TerraformCommand, Resettable {
    private String environment
    private static String resource
    private static String targetPath
    private static plugins = []
    private appliedPlugins = []

    TerraformImportCommand(String environment) {
        this.environment = environment
    }

    public String toString() {
        applyPlugins()
        if (resource) {
            if (targetPath) {
                def pieces = []
                pieces << 'terraform'
                pieces << 'import'
                pieces << targetPath
                pieces << resource

                return pieces.join(' ')
            }
            else {
                return "echo \"No target path set, skipping 'terraform import'."
            }
        }

        return "echo \"No resource set, skipping 'terraform import'."
    }

    public TerraformImportCommand withResource(String resource) {
        this.resource = resource
        return this
    }

    public String getTargetPath() {
        return this.targetPath
    }

    public TerraformImportCommand withTargetPath(String targetPath) {
        this.targetPath = targetPath
        return this
    }

    public String getResource() {
        return this.resource
    }

    public static void reset() {
        this.resource = ''
        this.targetPath = ''
        this.resetPlugins()
    }

    /**
     * Assures that all plugins are applied, and are applied at most once. It
     * can be safely called multiple times.
     */
    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformImportCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    /**
     * Accepts a plugin of the appropriate type and adds it to the list of plugins.
     *
     * @param plugin The plugin to add
     */
    public static void addPlugin(TerraformImportCommandPlugin plugin) {
        plugins << plugin
    }

    public static void setPlugins(plugins) {
        this.plugins = plugins
    }

    public static getPlugins() {
        return plugins
    }

    /**
     * Reset plugins will reset the plugin list to a default set of plugins.
     *
     * @param defaultPlugins list of plugins to set, default: []
     */
    public static void resetPlugins(defaultPlugins = []) {
        this.plugins = defaultPlugins.clone()
    }

    public static instanceFor(String environment) {
        return new TerraformImportCommand(environment)
    }

    public String getEnvironment() {
        return this.environment
    }
}
