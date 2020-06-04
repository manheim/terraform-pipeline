## [TerraformPlanResultsPR](../src/TerraformPlanResultsPR.groovy)

Use this to post Terraform plan results in the comments of a PR.

Configure usage of terraform landscape with the `withLandscape(bool)` method.

One-Time Setup:
* Install the terraform-landscape gem on your Jenkins slaves. (ONLY when using `withLandscape(true)`)

Requirements:
* Enable AnsiColorPlugin for colors in Jenkins (ONLY when usng `withLandscape(true)`)

```
@Library(['terraform-pipeline', 'terraform-pipeline-cai-plugins']) _
Jenkinsfile.init(this)
AnsiColorPlugin.init()                                      // REQUIRED: Decorate your TerraformEnvironmentStages with the AnsiColor plugin
TerraformPlanResultsPR.withLandscape(true).init()           // Post the plan results in the comments of a PR (landscape_gem used = true)
// OR TerraformPlanResultsPR.withLandscape(false).init()    // Post the plan results in the comments of a PR (landscape_gem used = false)
def validate = new TerraformValidateStage()
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')
validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```