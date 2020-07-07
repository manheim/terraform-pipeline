## [TerraformPlanResultsPRPlugin](../src/TerraformPlanResultsPRPlugin.groovy)

Use this to post Terraform plan results in the comments of a PR.

One-Time Setup:
* Install the terraform-landscape gem on your Jenkins agents. (ONLY when using `withLandscape(true)`)

Requirements:
* Set the environment variable for Github Authentication. The default environment variable to be set is `GITHUB_TOKEN`. The CredentialsPlugin can help with setting this variable.
* Enable AnsiColorPlugin for colors in Jenkins (ONLY when usng `withLandscape(true)`)

Configuration Methods:
* `withRepoHost(String)`: specify the Github source address (DEFAULT: "https://api.github.com/")
* `withRepoSlug(String)`: specify the full repo slug (DEFAULT: "")
* `withLandscape(bool)`: enable or disable the terraform landscape gem for plan output (DEFAULT: false)
* `withGithubTokenEnvVar(String)`: specify the environment variable used for github auth (DEFAULT: "GITHUB_TOKEN")

Important Notes:
* This plugin supports terraform plan output with the terraform landscape gem, but there is currently a standalone plugin `TerraformLandscapePlugin` for this feature.
* Note the `TerraformLandscapePlugin` does NOT contain support to post comments on pull requests.
* If you desire BOTH terraform landscape output and comments on PR's, you should use the `TerraformPlanResultsPRPlugin` with `withLandscape(true)`

```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

// set GITHUB_TOKEN environment variable for use with TerraformPlanResultsPRPlugin
// FOO/GITHUB_TOKEN will contain the respective username/password values of the 'my-cred' credential.
CredentialsPlugin.withBuildCredentials([usernameVariable: 'FOO', passwordVariable: 'GITHUB_TOKEN'], 'my-cred').init()

AnsiColorPlugin.init()                                    // Required when using 'withLandscape(true)' to colorize plan output
TerraformPlanResultsPRPlugin.withRepoHost("https://api.github.com/")
                            .withRepoSlug("my-org/my-repo")
                            .withLandscape(true)
                            .withGithubTokenEnvVar("GITHUB_TOKEN")
                            .init()

def validate = new TerraformValidateStage()
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')
validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
