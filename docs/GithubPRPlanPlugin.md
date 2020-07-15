## [GithubPRPlanPlugin](../src/GithubPRPlanPlugin.groovy)

Use this to post Terraform plan results in the comments of a PR.

One-Time Setup:
* Your pipeline should be configured as a Multibranch Pipeline with Github Repository HTTPS URL.
* Create a Github Personal Access Token that has permission to comment on your repo.  By default, the a Github Personal Access Token is expected to be available in an environment variable `GITHUB_TOKEN`.  A number of [credential and configuration management plugins](https://github.com/manheim/terraform-pipeline#credentials-and-configuration-management) are available to do this.  Optionally, your Github Authentication token can be assigned to a different environment variable using `.withGithubTokenEnvVar(<different variable>)`

```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

// A GITHUB_TOKEN environment variable should contain your Github PAT
GithubPRPlanPlugin.init()

// After creating a PullRequest, the plan results for each 
// environment are posted as a comment to the PullRequest.
def validate = new TerraformValidateStage()
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
