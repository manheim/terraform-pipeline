class TerraformTaintCommand implements TerraformCommand, Pluggable<TerraformTaintCommandPlugin> {
    private String command = "taint"
    private String resource
    private String environment

    public TerraformTaintCommand(String environment) {
        this.environment = environment
    }

    public TerraformTaintCommand withResource(String resource) {
        this.resource = resource

        return this
    }

    public String toString() {
        applyPlugins()
        if (resource) {
            def parts = []
            parts << 'terraform'
            parts << command
            parts << resource

            parts.removeAll { it == null }
            return parts.join(' ')
        }

        return "echo \"No resource set, skipping 'terraform taint'."
    }

    public String getResource() {
        return this.resource
    }

    public static TerraformTaintCommand instanceFor(String environment) {
        return new TerraformTaintCommand(environment)
    }

    public String getEnvironment() {
        return this.environment
    }
}
