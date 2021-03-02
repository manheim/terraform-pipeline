## [TerraformOutputOnlyPlugin](../src/TerraformOutputOnlyPlugin.groovy)

Enable this plugin to change pipeline functionality. This plugin will skip the plan and apply stages and add three new job parameters.

* `SHOW_OUTPUTS_ONLY`: This configures the job to skip execution of the plan and apply terraform commands. The job will perform the INIT stage and immediately perform `terraform output`. Unless this option is checked, the following options will have no effect.
* `JSON_FORMAT_OUTPUTS`: This will instruct the plugin to display the output in JSON format.
* `REDIRECT_OUTPUTS_TO_FILE`: Text entered into this option will be used to redirect the result of `terraform output` to a file in the current workspace. The filename should be relative to the workspace, and directories will NOT be created so they should exist beforehand.

```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// This enables the "output only" functionality
TerraformOutputOnlyPlugin.init()

def validate = new TerraformValidateStage()

def destroyQa = new TerraformEnvironmentStage('qa')
def destroyUat = new TerraformEnvironmentStage('uat')
def destroyProd = new TerraformEnvironmentStage('prod')

validate.then(destroyQa)
        .then(destroyUat)
        .then(destroyProd)
        .build()
```
