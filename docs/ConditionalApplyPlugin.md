## [ConditionalApplyPlugin](../src/ConditionalApplyPlugin.groovy)

This plugin is enabled by default.

Changes should be applied through one and only one branch - master.  The ConditionalApplyPlugin enforces this by making the "Confirm" and "Apply" steps of a TerraformEnvironmentStage visible only on the master branch.  You can continue to use branches and PullRequests, however, branches and PullRequests will only run the Plan step for each environment, and skip over the Confirm/Apply steps.

Disable this plugin, if you want to allow "Confirm" and "Apply" on any branch or PullRequest.

```
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)
ConditionalApplyPlugin.disable()
...
```
