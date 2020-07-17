class TerraformApplyCommand {
    protected boolean input = false
    protected String terraformBinary = "terraform"
    protected String command
    String environment
    protected prefixes = []
    protected suffixes = []
    protected args = []
    protected static plugins = []
    protected appliedPlugins = []
    protected String directory

    public TerraformApplyCommand(String environment, String command = "apply") {
        this.environment = environment
        this.command = command
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

    protected applyPluginsOnce() {
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
