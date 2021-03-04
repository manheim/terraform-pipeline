class TerraformTaintCommand implements TerraformCommand, Pluggable<TerraformTaintCommandPlugin>, Resettable {
    private String command = "taint"
    private String resource

    public TerraformTaintCommand(String environment) {
        this.environment = environment
    }

    public TerraformTaintCommand withResource(String resource) {
        this.resource = resource

        return this
    }

    public String assembleCommandString() {
        if (resource) {
            def parts = []
            parts << terraformBinary
            parts << command
            parts << resource

            parts.removeAll { it == null }
            return parts.join(' ')
        }

        return "echo \"No resource set, skipping 'terraform taint'."
    }

    public static reset() {
        this.plugins = []
    }

    public String getResource() {
        return this.resource
    }

    public static TerraformTaintCommand instanceFor(String environment) {
        return new TerraformTaintCommand(environment)
    }
}
