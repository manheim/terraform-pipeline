## [TerraformTaintPlugin](../src/TerraformTaintPlugin.groovy)

Enable this plugin to add `TAINT_RESOURCE` and `UNTAINT_RESOURCE` parameters
to the build. If a resource path is provided via one of those parameters, then
the `terraform plan` command will be preceded by the corresponding Terraform
command to taint or untaint the appropriate resources.

Note that the untaint command will take precedence, so if for some reason the
same resource is placed in both parameters, it will be tainted and immediately
untainted, resulting in no change.

There are several ways to customize where and when the taint/untaint can run:

* `onlyOnOriginRepo()`: This method takes in a string of the form
  `<userOrOrg>/<repoName>`. When specified, the plugin will only apply when
  the current checkout is from the same repository and not a fork or other
  clone.
* `onMasterOnly()`: This takes no arguments. This restricts the plugin from
  applying to any branch other than master. This is the default behavior.
* `onBranch()`: This takes in a branch name as a parameter. This adds the
  branch to the list of approved branches.

```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// This enables the "taint" and "untaint" functionality
// It will only apply to the master branch (default behavior)
// and will only apply if the current worspace code is checked out from the
// MyOrg/myrepo repository.
TerraformTaintPlugin.init()
TerraformTaintPlugin.onlyOnOriginRepo("MyOrg/myrepo")

def validate = new TerraformValidateStage()

def destroyQa = new TerraformEnvironmentStage('qa')
def destroyUat = new TerraformEnvironmentStage('uat')
def destroyProd = new TerraformEnvironmentStage('prod')

validate.then(destroyQa)
        .then(destroyUat)
        .then(destroyProd)
        .build()
```
