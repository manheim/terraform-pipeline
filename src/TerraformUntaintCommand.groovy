class TerraformUntaintCommand implements TerraformCommand, Pluggable<TerraformUntaintCommandPlugin> {
    private String command = "untaint"
    private String resource
    private String environment

    public TerraformUntaintCommand(String environment) {
        this.environment = environment
    }

    public TerraformUntaintCommand withResource(String resource) {
        this.resource = resource
        return this
    }

    public String toString() {
        applyPlugins()
        def parts = []
        parts << 'terraform'
        parts << command
        parts << resource

        parts.removeAll { it == null }
        return parts.join(' ')
    }

    public String getResource() {
        return this.resource
    }

    public static TerraformUntaintCommand instanceFor(String environment) {
        return new TerraformUntaintCommand(environment)
    }

    public String getEnvironment() {
        return this.environment
    }
}

