## [CrqPlugin](../src/CrqPlugin.groovy)

Enable this plugin to wrap your terraform apply step with an automated Change Request, using the manheim_remedier ruby gem.

One-time setup:
* Install the manheim_remedier gem on your Jenkins slaves.
* Optional: Define global variables that match your environment name, to enable automated Change Requests across all pipelines with that environment:
  * UAT_CRQ_ENVIRONMENT = 'Cloud-CRQ' (all 'uat' environments will be assigned a CRQ_ENVIRONMENT = 'Cloud-CRQ')
  * PROD_CRQ_ENVIRONMENT = 'Cloud' (all 'prod' environments will be assigned a CRQ_ENVIRONMENT = 'Cloud')
* Define the following environment variables, which will be used when submitting a Change Request:
  * DEFAULT_PIPELINE_CRQ_FIRST_NAME
  * DEFAULT_PIPELINE_CRQ_LAST_NAME
  * DEFAULT_PIPELINE_CRQ_LOGIN

An automated Change Request will only be created for environments that specify a `CRQ_ENVIRONMENT`, or match a global `<environment>_CRQ_ENVIRONMENT`.  For each such environments, a Change Request will be opened before applying the terraform plan, then closed and marked as either successful or failed, depending on the result of `terraform apply`.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

CrqPlugin.init() // Enable AutoCRQ's

def validate = new TerraformValidateStage()

// Do not define either a CRQ_ENVIRONMENT or a QA_CRQ_ENVIRONMENT, so that a Change Request isn't created.
def deployQA = new TerraformEnvironmentStage('qa')

// Define a global UAT_CRQ_ENVIRONMENT, or set a uat-specific CRQ_ENVIRONMENT variable to trigger a Change Request here
def deployUat = new TerraformEnvironmentStage('uat')

// Define a global PROD_CRQ_ENVIRONMENT, or set a prod-specific CRQ_ENVIRONMENT variable to trigger a Change Request here
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
