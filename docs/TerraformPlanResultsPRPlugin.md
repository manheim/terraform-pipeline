## [TerraformPlanResultsPRPlugin](../src/TerraformPlanResultsPRPlugin.groovy)

Use this to post Terraform plan results in the comments of a PR.

Requirements:
* Set the environment variable for Github Authentication. The default environment variable to be set is `GITHUB_TOKEN`. The CredentialsPlugin can help with setting this variable.

Configuration Methods:
* `withRepoHost(String)`: specify the Github source address (DEFAULT: "https://api.github.com/")
* `withRepoSlug(String)`: specify the full repo slug (DEFAULT: "")
* `withGithubTokenEnvVar(String)`: specify the environment variable used for github auth (DEFAULT: "GITHUB_TOKEN")

```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

// set GITHUB_TOKEN environment variable for use with TerraformPlanResultsPRPlugin
// FOO/GITHUB_TOKEN will contain the respective username/password values of the 'my-cred' credential.
CredentialsPlugin.withBuildCredentials([usernameVariable: 'FOO', passwordVariable: 'GITHUB_TOKEN'], 'my-cred').init()

TerraformPlanResultsPRPlugin.withRepoHost("https://api.github.com/")
                            .withRepoSlug("my-org/my-repo")
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
