## [TerraformPlanResultsPR](../src/TerraformPlanResultsPR.groovy)

Use this to post Terraform plan results in the comments of a PR.

One-Time Setup:
* Install the terraform-landscape gem on your Jenkins slaves. (ONLY when using `withLandscape(true)`)

Requirements:
* Enable CredentialsPlugin to set the GITHUB_TOKEN environment varaible. This is later used in the TerraformPlanResultsPR Plugin for authentication.
* Enable AnsiColorPlugin for colors in Jenkins (ONLY when usng `withLandscape(true)`)

Configuration Methods:
* `withRepoHost(String)`: specify the Github source address (DEFAULT: "https://api.github.com/")
* `withRepoSlug(String)`: specify the full repo slug (DEFAULT: "")
* `withLandscape(bool)`: enable or disable the terraform landscape gem for plan output (DEFAULT: false)
* `withGithubTokenEnvVar(String)`: specify the environment variable used for github auth (DEFAULT: "GITHUB_TOKEN")


```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

// Required to set GITHUB_TOKEN envionrment variable for use with TerraformPlanResultsPR Plugin
// FOO/GITHUB_TOKEN will contain the respective username/password values of the 'my-cred' credential.
CredentialsPlugin.withBuildCredentials([usernameVariable: 'FOO', passwordVariable: 'GITHUB_TOKEN'], 'my-cred').init()

AnsiColorPlugin.init()                                           // Required when using 'withLandscape(true)'
TerraformPlanResultsPR.withRepoHost("https://api.github.com/")
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