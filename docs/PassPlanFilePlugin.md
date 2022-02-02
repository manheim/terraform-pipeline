## [PassPlanFilePlugin](../src/PassPlanFilePlugin.groovy)

Enable this plugin to pass the plan file output to `terraform apply`.

This plugin stashes the plan file during the `plan` step.
When `apply` is called, the plan file is unstashed and passed as an argument.

For stash and unstash commands, you can either specify a directory when initializing the plugin, or it will default to the `./` directory.
If you are using the TerraformDirectoryPlugin, you must specify the same directory to support stash and unstash.


```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// Pass the plan file to 'terraform apply'
PassPlanFilePlugin.init()

// When using TerraformDirectoryPlugin,
// Pass the plan file to 'terraform apply' using withDirectory
PassPlanFilePlugin.withDirectory('./tf/').init()
TerraformDirectoryPlugin.withDirectory('./tf/').init()


def validate = new TerraformValidateStage()

def destroyQa = new TerraformEnvironmentStage('qa')
def destroyUat = new TerraformEnvironmentStage('uat')
def destroyProd = new TerraformEnvironmentStage('prod')

validate.then(destroyQa)
        .then(destroyUat)
        .then(destroyProd)
        .build()
```
