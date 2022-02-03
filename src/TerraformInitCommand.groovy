class TerraformInitCommand implements TerraformCommand, Resettable {
    private boolean input = false
    private String terraformBinary = "terraform"
    private String command = "init"
    String environment
    private prefixes = []
    private suffixes = []
    private backendConfigs = []
    private boolean doBackend = true
    private String directory
    private boolean chdir_flag = false
    private static plugins = []
    private appliedPlugins = []

    public TerraformInitCommand(String environment) {
        this.environment = environment
    }

    public TerraformInitCommand withInput(boolean input) {
        this.input = input
        return this
    }

    public TerraformInitCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformInitCommand withSuffix(String suffix) {
        suffixes << suffix
        return this
    }

    public TerraformInitCommand withDirectory(String directory) {
        this.directory = directory
        return this
    }

    public TerraformInitCommand withChangeDirectoryFlag() {
        this.chdir_flag = true
        return this
    }

    public TerraformInitCommand withBackendConfig(String backendConfig) {
        this.backendConfigs << backendConfig
        return this
    }

    public TerraformInitCommand withoutBackend() {
        this.doBackend = false
        return this
    }

    public String toString() {
        applyPlugins()
        def pieces = []
        pieces += prefixes
        pieces << terraformBinary
        if (directory && chdir_flag) {
            pieces << "-chdir=${directory}"
        }
        pieces << command
        if (!input) {
            pieces << "-input=false"
        }
        if (doBackend) {
            backendConfigs.each { config ->
                pieces << "-backend-config=${config}"
            }
        } else {
            pieces << "-backend=false"
        }
        if (directory && !chdir_flag) {
            pieces << directory
        }

        pieces += suffixes

        return pieces.join(' ')
    }

    public static TerraformInitCommand instanceFor(String environment) {
        return new TerraformInitCommand(environment)
    }

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformInitCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformInitCommandPlugin plugin) {
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
