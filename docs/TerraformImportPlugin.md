## [TerraformImportPlugin](../src/TerraformImportPlugin.groovy)

Enable this plugin to change pipeline functionality. This plugin will import a
resource into state, and adds two new job parameters.

* `IMPORT_RESOURCE`: This is the resource identifier to import into state.
* `IMPORT_TARGET_PATH`: This is the Terraform state path into which the
  resource should be imported.
* `IMPORT_ENVIRONMENT`: This should be set to the terraform-pipeline
  environment stage that should perform the import.

```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// This enables the "import" functionality
TerraformImportPlugin.init()

def validate = new TerraformValidateStage()

def qa = new TerraformEnvironmentStage('qa')
def uat = new TerraformEnvironmentStage('uat')
def prod = new TerraformEnvironmentStage('prod')

validate.then(qa)
        .then(uat)
        .then(prod)
        .build()
```
