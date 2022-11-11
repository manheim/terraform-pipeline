## [AnsiColorPlugin](../src/AnsiColorPlugin.groovy)

Enable this plugin to color the output for terraform plan and apply.

One-time setup:
* Install the [AnsiColorPlugin](https://wiki.jenkins.io/display/JENKINS/AnsiColor+Plugin) on your Jenkins server.

```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

AnsiColorPlugin.init() // Decorate your TerraformEnvironmentStages with the AnsiColor plugin

def validate = new TerraformValidateStage()

def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
