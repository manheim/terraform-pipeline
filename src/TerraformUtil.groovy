class TerraformUtil implements Serializable {
    def steps
    static SemanticVersion version
    static final String DEFAULT_VERSION = '0.11.14'
    public static TERRAFORM_VERSION_FILE = '.terraform-version'

    public SemanticVersion detectVersion() {
      if(version == null) {
        if (fileExists(TERRAFORM_VERSION_FILE)) {
          version = new SemanticVersion(readFile(TERRAFORM_VERSION_FILE))
        } else {
          version = new SemanticVersion(DEFAULT_VERSION)
        }
      }

      return version
    }
}