class TerraformValidateCommand implements Resettable {
    private String terraformBinary = "terraform"
    private String command = "validate"
    private arguments = []
    private prefixes = []
    private suffixes = []
    private static globalPlugins = []
    private appliedPlugins = []
    private String directory

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

    public String toString() {
        applyPluginsOnce()
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

    private applyPluginsOnce() {
        def remainingPlugins = globalPlugins - appliedPlugins

        for (TerraformValidateCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static addPlugin(TerraformValidateCommandPlugin plugin) {
        this.globalPlugins << plugin
    }

    public static TerraformValidateCommand instance() {
        return new TerraformValidateCommand()
    }

    public static getPlugins() {
        return this.globalPlugins
    }

    public static reset() {
        this.globalPlugins = []
    }
}
