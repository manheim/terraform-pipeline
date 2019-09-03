## [TerraformDirectoryPlugin](../src/TerraformDirectoryPlugin.groovy)

This plugin allows Terraform to run in a specific directory so that the number of files at the root of any given project can be limited.

It works by appending the directory to the end of any Terraform command run by terraform-pipeline. You can either specify a directory when initializing the plugin, or it will default to the `./terraform/` directory.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

// Using withDirectory to initialize here would cause all terraform
// commands to run in the ./xyz/ directory
TerraformDirectoryPlugin.withDirectory('./xyz/').init()

def validate = new TerraformValidateStage()

def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
