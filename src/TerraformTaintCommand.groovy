class TerraformTaintCommand implements TerraformCommand {
  private static DEFAULT_BRANCHES = ['master']
  private static branches = DEFAULT_BRANCHES

  String environment
  private String originRepoSlug = ""
  private String terraformBinary = "terraform"
  private String command = "taint"
  private String args = []
  private static plugins = []
  private appliedPlugins = []

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

  public String getEnvironment() {
    return this.environment
  }
}