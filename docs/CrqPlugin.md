## [CrqPlugin](../src/CrqPlugin.groovy)

Enable this plugin to wrap your terraform apply step with an automated Change Request, using the jenkins-crq library.

One-time setup:
* Install the jenkins-crq-library. Using this library requires a one-time setup as a shared library in your Jenkins instance.
* Define the following environment variables, which will be used when submitting a Change Request:
   * `CRQ_COMPONENT` OR `CRQ_COMPONENT_ID`
   * `CRQ_SHORT_DESCRIPTION`
   * `CRQ_DESCRIPTION`
   * `CRQ_NOTES`       (optional)
   * `CRQ_WORK_NOTES`  (optional) - comma-seperated string of work notes to add to the CRQ (defaults to '')
   * `CRQ_SANDBOX`     (optional) - defaults to `false`
   * `CRQ_AUTH_METHOD` (optional) - All steps can take a auth_method parameter, which defaults to `machine_auth` if unset.

For each environment, a Change Request will be opened before applying the terraform plan, then closed and marked as either successful or failed, depending on the result of `terraform apply`.

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
