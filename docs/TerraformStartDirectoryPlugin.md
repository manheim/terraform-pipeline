## [TerraformStartDirectoryPlugin](../src/TerraformStartDirectoryPlugin.groovy)

This plugin changes the starting execution workspace directory. Commands will use this directory as current and any relative path will use it as base path.

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

TerraformStartDirectoryPlugin.withDirectory('./xyz/').init()

def validate = new TerraformValidateStage()

def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
