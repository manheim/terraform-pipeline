## [ConditionalApplyPlugin](../src/ConditionalApplyPlugin.groovy)

This plugin is enabled by default.

By default, changes are applied through one and only one branch - main.  The
ConditionalApplyPlugin enforces this by making the "Confirm" and "Apply" steps
of a TerraformEnvironmentStage visible only on the main or master branch.  You can
continue to use branches and PullRequests, however, branches and PullRequests
will only run the Plan step for each environment, and skip over the
Confirm/Apply steps.

This behavior can be changed by using `ConditionalApplyPlugin.withApplyOnBranch()`.  This method accepts one or more branches.  "Confirm" and "Apply" steps of TerraformEnvironmentStage will then be visible for each of the specified branches.  Any branch or PullRequest not in that list will only run the Plan step for each environment, and skip over the Confirm/Apply steps.

Example:

```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)
ConditionalApplyPlugin.withApplyOnBranch('myMainReplacement')

def validate = new TerraformValidateStage()
// 'qa' stage will run Plan/Confirm/Apply on the 'myMainReplacement' branch.
// 'qa' stage will only run Plan for all other branches and PullRequests.
def deployQa = new TerraformEnvironmentStage('qa')
// 'uat' stage will run Plan/Confirm/Apply on 'myMainReplacement' branch.
// 'uat' stage will only run Plan for all other branches and PullRequests.
def deployUat = new TerraformEnvironmentStage('uat')
// 'prod' stage will run Plan/Confirm/Apply on 'myMainReplacement' branch.
// 'prod' stage will only run Plan for all other branches and PullRequests.
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
...
```

Alternatively, enable "Confirm" and "Apply" for specific environments with `ConditionalApplyPlugin.withApplyOnEnvironment()`.  This method accepts one or more environment names.  "Confirm" and "Apply" steps of TerraformEnvironmentStage will then be visible for each of the specified environments, regardless of the branch or PullRequest.

Example:

```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)
ConditionalApplyPlugin.withApplyOnEnvironment('qa')

def validate = new TerraformValidateStage()
// 'qa' stage will run Plan/Confirm/Apply on all branches and PullRequests.
def deployQa = new TerraformEnvironmentStage('qa')
// 'uat' stage will run Plan/Confirm/Apply only on main, and will only run Plan on all other branches and PullRequests.
def deployUat = new TerraformEnvironmentStage('uat')
// 'prod' stage will run Plan/Confirm/Apply only on main, and will only run Plan on all other branches and PullRequests.
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
...
```

Disable this plugin, if you want to allow "Confirm" and "Apply" on any branch or PullRequest.

```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)
ConditionalApplyPlugin.disable()
...
```
