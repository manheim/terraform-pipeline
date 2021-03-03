trait TerraformCommand {
    private String environment
    private String terraformBinary = "terraform"

    public String getEnvironment() {
        return environment
    }

    public void setEnvironment(String environment) {
        this.environment = environment
    }

    public void setTerraformBinary(String terraformBinary) {
        this.terraformBinary = terraformBinary
    }

    public String getTerraformBinary() {
        return this.terraformBinary
    }

    public abstract String assembleCommandString()

    public String toString() {
        return this.assembleCommandString()
    }
}
