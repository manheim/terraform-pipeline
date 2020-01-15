## [AwssumePlugin](../src/AwssumePlugin.groovy)

Enable this plugin to wrap your terraform commands with the [awssume](https://github.com/manheim/awssume) gem, allowing you to assume roles across accounts.

One-time setup:
* Install the awssume gem on your Jenkins slaves.
* Optional: Define global variables that match your environment name, to a role across all pipelines with that environment:
  * QA_AWS_ROLE_ARN (all 'qa' environments will assume the role specified by this variable)
  * UAT_AWS_ROLE_ARN (all 'uat' environments will assume the role specified by this variable)
  * PROD_AWS_ROLE_ARN (all 'prod' environments will assume the role specified by this variable)

Awssume will assume the role for any environment where a `AWS_ROLE_ARN` is defined, or for any environment that matches a global `<environment>_AWS_ROLE_ARN`.  If neither variables are specified, the use of Awssume will be skipped.

```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

AwssumePlugin.init() // Decorate your TerraformEnvironmentStages with the Awssume plugin

def validate = new TerraformValidateStage()

// Run terraform apply and plan using the AWS Role defined by either AWS_ROLE_ARN or QA_AWS_ROLE_ARN
def deployQA = new TerraformEnvironmentStage('qa')

// Run terraform apply and plan using the AWS Role defined by either AWS_ROLE_ARN or UAT_AWS_ROLE_ARN
def deployUat = new TerraformEnvironmentStage('uat')

// Run terraform apply and plan using the AWS Role defined by either AWS_ROLE_ARN or PROD_AWS_ROLE_ARN
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
