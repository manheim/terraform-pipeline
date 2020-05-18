## [TerraformLandscapePlugin](../src/TerraformLandscapePlugin.groovy)

Enable this plugin to improve Terraform's plan output commands with the [terraform-landscape](https://github.com/coinbase/terraform-landscape) gem.

One-time setup:
* Install the terraform-landscape gem on your Jenkins slaves.

Requirements:
* Enable AnsiColorPlugin for colors in Jenkins

```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

AnsiColorPlugin.init()          // REQUIRED: Decorate your TerraformEnvironmentStages with the AnsiColor plugin
TerraformLandscapePlugin.init() // Use the Terraform Landscape gem to format plan output

def validate = new TerraformValidateStage()

def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
