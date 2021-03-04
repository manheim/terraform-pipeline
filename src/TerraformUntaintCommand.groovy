class TerraformUntaintCommand implements TerraformCommand, Pluggable<TerraformUntaintCommandPlugin>, Resettable {
  private String command = "untaint"
  private String resource

  public TerraformUntaintCommand(String environment) {
    this.environment = environment
  }

  public TerraformUntaintCommand withResource(String resource) {
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

    return "echo \"No resource set, skipping 'terraform untaint'."
  }

  public static reset() {
    this.plugins = []
  }

  public String getResource() {
    return this.resource
  }
}

