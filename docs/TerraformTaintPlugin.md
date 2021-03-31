## [TerraformTaintPlugin](../src/TerraformTaintPlugin.groovy)

Enable this plugin to add `TAINT_RESOURCE` and `UNTAINT_RESOURCE` parameters
to the build. If a resource path is provided via one of those parameters, then
the `terraform plan` command will be preceded by the corresponding Terraform
command to taint or untaint the appropriate resources.

Note that the untaint command will take precedence, so if for some reason the
same resource is placed in both parameters, it will be tainted and immediately
untainted, resulting in no change.

There are several ways to customize where and when the taint/untaint can run:

* `onBranch()`: This takes in a branch name as a parameter. This adds the
  branch to the list of approved branches.

```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// This enables the "taint" and "untaint" functionality
// It will only apply to the master branch (default behavior)
TerraformTaintPlugin.init()

def validate = new TerraformValidateStage()

def destroyQa = new TerraformEnvironmentStage('qa')
def destroyUat = new TerraformEnvironmentStage('uat')
def destroyProd = new TerraformEnvironmentStage('prod')

validate.then(destroyQa)
        .then(destroyUat)
        .then(destroyProd)
        .build()
```
