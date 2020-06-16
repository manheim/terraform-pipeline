## [TargetPlugin](../src/TargetPlugin.groovy)

Enable this plugin to run plan/apply on selective resource targets

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this, env)
TargetPlugin.init() // Optionally limit plan/apply to specific targets


def validate = new TerraformValidateStage()

def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')


// Pipeline can now be built with "Build with Parameters"
//     New 'target' option in parameter list can limit plan/apply to specific targets
//     By default, builds should skip the 'target' option, and build as-normal
validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
