class TerraformValidateCommand implements Resettable{
    private String terraformBinary = "terraform"
    private String command = "validate"
    private arguments = []
    private prefixes = []
    private suffixes = []
    private String directory
    private static plugins = []
    private appliedPlugins = []

    public TerraformValidateCommand() {
    }

    public TerraformValidateCommand withArgument(String argument) {
        this.arguments << argument
        return this
    }

    public TerraformValidateCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformValidateCommand withSuffix(String suffix) {
        suffixes << suffix
        return this
    }

    public TerraformValidateCommand withDirectory(String directory) {
        this.directory = directory
        return this
    }

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (T plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(T plugin) {
        plugins << plugin
    }

    public static void setPlugins(plugins) {
        this.plugins = plugins
    }

    public static getPlugins() {
        return plugins
    }

    public static void resetPlugins(defaultPlugins = []) {
        this.plugins = defaultPlugins.clone()
    }

    public static void reset() {
        this.resetPlugins()
    }

    public String toString() {
        applyPlugins()
        def pieces = []
        pieces = pieces + prefixes
        pieces << terraformBinary
        pieces << command
        for (String argument in arguments) {
            pieces << argument
        }
        if (directory) {
            pieces << directory
        }
        pieces += suffixes

        return pieces.join(' ')
    }

    public static TerraformValidateCommand instance() {
        return new TerraformValidateCommand()
    }
}
