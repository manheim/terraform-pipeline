## [FormatPlugin](../src/FormatPlugin.groovy)

Enable this plugin to run `terraform fmt -check` as part of the TerraformValidateStage.  If no changes are necessary, TerraformValidateStage will pass.  If any format changes are necessary, the TerraformValidateStage will fail.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

FormatPlugin.init()

// Runs `terraform fmt -check` in addition to `terraform validate`.
// TerraformValidateStage fails if code requires validation.
def validate = new TerraformValidateStage()
def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```

Additonal options are available, to search directories recusively, and to display diffs.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

FormatPlugin.withRecursive()
            .withDiff()
            .init()

// Runs `terraform fmt -check` in addition to `terraform validate`.
// TerraformValidateStage fails if code requires validation.
def validate = new TerraformValidateStage()
def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```

