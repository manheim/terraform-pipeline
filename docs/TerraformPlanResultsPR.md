## [TerraformPlanResultsPR](../src/TerraformPlanResultsPR.groovy)

Use this to post Terraform plan results in the comments of a PR.

Configure usage of terraform landscape with the `withLandscape(bool)` method.

One-Time Setup:
* Install the terraform-landscape gem on your Jenkins slaves. (ONLY when using `withLandscape(true)`)

Requirements:
* Enable CredentialsPlugin to set the GITHUB_TOKEN environment varaible. This is later used in the TerraformPlanResultsPR Plugin for authentication.
* Enable AnsiColorPlugin for colors in Jenkins (ONLY when usng `withLandscape(true)`)

```
@Library(['terraform-pipeline', 'terraform-pipeline-cai-plugins']) _

Jenkinsfile.init(this)

// FOO/GITHUB_TOKEN will contain the respective username/password values of the 'my-cred' credential.
CredentialsPlugin.withBuildCredentials([usernameVariable: 'FOO', passwordVariable: 'GITHUB_TOKEN'], 'my-cred').init()

AnsiColorPlugin.init()                                                               // REQUIRED: Decorate your TerraformEnvironmentStages with the AnsiColor plugin
TerraformPlanResultsPR.withRepoHost("https://github.com/api/v3/")
                      .withRepoSlug("my-org/my-repo")
                      .withLandscape(true)
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