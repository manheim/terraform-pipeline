class TerraformDestroyCommand {
    private boolean input = false
    private String terraformBinary = "terraform"
    private String command = "destroy"
    String environment
    private prefixes = []
    private suffixes = []
    private args = []
    private static plugins = []
    private appliedPlugins = []
    private String directory

    public TerraformDestroyCommand(String environment) {
        this.environment = environment
    }

    public TerraformDestroyCommand withInput(boolean input) {
        this.input = input
        return this
    }

    public TerraformDestroyCommand withArgument(String arg) {
        this.args << arg
        return this
    }

    public TerraformDestroyCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformDestroyCommand withSuffix(String suffix) {
        suffixes << suffix
        return this
    }

    public TerraformDestroyCommand withDirectory(String directory) {
        this.directory = directory
        return this
    }

    public String toString() {
        applyPluginsOnce()

        def pieces = []
        pieces += prefixes
        pieces << terraformBinary
        pieces << command
        if (!input) {
            pieces << "-input=false"
        }
        pieces += args
        if (directory) {
            pieces << directory
        }

        pieces += suffixes

        return pieces.join(' ')
    }

    private applyPluginsOnce() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformDestoryCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformDestroyCommandPlugin plugin) {
        plugins << plugin
    }

    public static TerraformDestroyCommand instanceFor(String environment) {
        return new TerraformDestroyCommand(environment)
            .withInput(false)
            .withArgument("-auto-approve")
    }

    public static getPlugins() {
        return plugins
    }

    public static resetPlugins() {
        this.plugins = []
    }
}
