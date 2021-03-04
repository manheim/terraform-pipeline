class TerraformTaintCommand implements TerraformCommand, Pluggable<TerraformTaintCommandPlugin> {
  private static DEFAULT_BRANCHES = ['master']
  private static branches = DEFAULT_BRANCHES

  private String originRepoSlug = ""
  private String command = "taint"
  private String resource

  public TerraformTaintCommand(String environment) {
    this.environment = environment
    this.plugins = BuildWithParametersPlugin
  }

  public TerraformTaintCommand withOriginRepo(String originRepoSlug) {
    this.originRepoSlug = originRepoSlug
    return this
  }

  public TerraformTaintCommand onMasterOnly() {
    this.branches = ['master']
    return this
  }

  public TerraformTaintCommand onAnyBranch() {
    this.branches = ['any']
    return this
  }

  public TerraformTaintCommand onBranch(String branchName) {
    this.branches << branchName
    return this
  }

  public TerraformTaintCommand withResource(String resource) {
    this.resource = resource
  }

  public String getEnvironment() {
    return this.environment
  }

  public String assembleCommandString() {
    def parts = []
    parts << terraformBinary
    parts << command
    parts << resource

    parts.removeAll { it == null }
    return parts.join(' ')
  }
}

