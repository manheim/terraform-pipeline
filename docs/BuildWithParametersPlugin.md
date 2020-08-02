## [BuildWithParametersPlugin](../src/BuildWithParametersPlugin.groovy)

This plugin is enabled by default.

Pipelines can prompt the user for parameters at build-time, with the Jenkinsfile "Build With Parameters" feature.  This plugin lets you enable this feature, and prompt the user for parameters.  If no parameters are configured, this plugin does nothing.

Eg:

```
@Library(['terraform-pipeline@v5.1']) _

Jenkinsfile.init(this)
BuildWithParametersPlugin.withBooleanParameter([
       name: 'PIPELINE_PREFERENCE',
       description: 'Do you like pipelines?',
       defaultValue: true
     ])
BuildWithParametersPlugin.withStringParameter([
       name: 'PIPELINE_THOUGHTS',
       description: 'What do you think about pipelines?',
       defaultValue: 'They make deployments so easy'
     ])

def validate = new TerraformValidateStage()
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

// At the start of the pipeline, the user with a checkbox and a string input
// The user's responses are available in the environment variables
// PIPELINE_PREFERENCE and PIPELINE_THOUGHTS
validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
