class TerraformImportCommand implements TerraformCommand, Pluggable<TerraformImportCommandPlugin> {
    private String environment
    private static String resource
    private static String targetPath

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
        resetPlugins()
    }

    public static instanceFor(String environment) {
        return new TerraformImportCommand(environment)
    }

    public String getEnvironment() {
        return this.environment
    }
}
