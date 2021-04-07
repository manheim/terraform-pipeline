class TerraformOutputCommand implements TerraformCommand, Resettable {
    private String command = "output"
    private String terraformBinary = "terraform"
    String environment
    private boolean json = false
    private String redirectFile
    private String stateFilePath
    private static plugins = []
    private appliedPlugins = []

    public TerraformOutputCommand(String environment) {
        this.environment = environment
    }

    public TerraformOutputCommand withJson(boolean json) {
        this.json = json
        return this
    }

    public TerraformOutputCommand withRedirectFile(String redirectFile) {
        this.redirectFile = redirectFile
        return this
    }

    public String toString() {
        applyPlugins()
        def pieces = []
        pieces << terraformBinary
        pieces << command

        if (json) {
            pieces << "-json"
        }

        if (redirectFile) {
            pieces << ">${redirectFile}"
        }

        return pieces.join(' ')
    }

    public static TerraformOutputCommand instanceFor(String environment) {
        return new TerraformOutputCommand(environment).withJson(false)
    }

    public String getEnvironment() {
        return environment
    }

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformOutputCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformOutputCommandPlugin plugin) {
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
