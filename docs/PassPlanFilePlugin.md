## [PassPlanFilePlugin](../src/PassPlanFilePlugin.groovy)

Enable this plugin to pass the plan file output to `terraform apply`.

This plugin stashes the plan file during the `plan` step.
When `apply` is called, the plan file is unstashed and passed as an argument.


```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// Pass the plan file to 'terraform apply'
PassPlanFilePlugin.init()

def validate = new TerraformValidateStage()

def destroyQa = new TerraformEnvironmentStage('qa')
def destroyUat = new TerraformEnvironmentStage('uat')
def destroyProd = new TerraformEnvironmentStage('prod')

validate.then(destroyQa)
        .then(destroyUat)
        .then(destroyProd)
        .build()
```
