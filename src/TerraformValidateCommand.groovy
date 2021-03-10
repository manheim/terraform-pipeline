class TerraformValidateCommand implements Pluggable<TerraformValidateCommandPlugin>, Resettable {
    private String terraformBinary = "terraform"
    private String command = "validate"
    private arguments = []
    private prefixes = []
    private suffixes = []
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

    public static reset() {
        this.plugins = []
    }
}
