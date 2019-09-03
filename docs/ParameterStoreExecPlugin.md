## [ParameterStoreExecPlugin](../src/ParameterStoreExecPlugin.groovy)

Enable this plugin to inject variables from AWS ParameterStore using [parameter-store-exec](https://github.com/cultureamp/parameter-store-exec).

Prefer using the [ParameterStoreBuildWrapperPlugin](#ParameterStoreBuildWrapperPlugin) above if possible.

One-time setup:
* Install parameter-store-exec on your Jenkins slaves.

By default, parameters will be retrieved from the ParameterStore path constructed from your project's Git Organization, Git Repository name, and environment.  Eg: If my terraform project were at https://github.com/Manheim/fake-terraform-project, then my 'qa' environment would receive parameters from the ParameterStore path '/Manheim/fake-terraform-project/qa'.

```
// Jenkinsfile
@Library(['terraform-pipeline@v']) _

Jenkinsfile.init(this)

ParameterStoreExecPlugin.init() // Enable ParameterStoreExecPlugin

def validate = new TerraformValidateStage()

// Inject all parameters in /<GitOrg>/<GitRepo>/qa with parameter-store-exec
def deployQA = new TerraformEnvironmentStage('qa')

// Inject all parameters in /<GitOrg>/<GitRepo>/uat with parameter-store-exec
def deployUat = new TerraformEnvironmentStage('uat')

// Inject all parameters in /<GitOrg>/<GitRepo>/prod with parameter-store-exec
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
