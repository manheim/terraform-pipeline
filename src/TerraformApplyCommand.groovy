class TerraformApplyCommand {
    private boolean input = false
    private String terraformBinary = "terraform"
    private String command
    String environment
    private prefixes = []
    private suffixes = []
    private args = []
    private static plugins = []
    private appliedPlugins = []
    private String directory

    public TerraformApplyCommand(String environment) {
        this.environment = environment
        this.command = "apply"
    }

    public TerraformApplyCommand withInput(boolean input) {
        this.input = input
        return this
    }

    public TerraformApplyCommand withArgument(String arg) {
        this.args << arg
        return this
    }

    public TerraformApplyCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformApplyCommand withSuffix(String suffix) {
        suffixes << suffix
        return this
    }

    public TerraformApplyCommand withDirectory(String directory) {
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

        for (TerraformApplyCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformApplyCommandPlugin plugin) {
        plugins << plugin
    }

    public static TerraformApplyCommand instanceFor(String environment) {
        return new TerraformApplyCommand(environment)
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
