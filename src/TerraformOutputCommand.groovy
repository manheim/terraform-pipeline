class TerraformOutputCommand implements TerraformCommand, Pluggable<TerraformOutputCommandPlugin> {
    private String command = "output"
    private String terraformBinary = "terraform"
    String environment
    private boolean json = false
    private String redirectFile
    private String stateFilePath

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
}
