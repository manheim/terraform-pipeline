## [ParameterStoreBuildWrapperPlugin](../src/ParameterStoreBuildWrapperPlugin.groovy)

Enable this plugin to inject variables using the [AWS Parameter Store Build Wrapper Plugin](https://plugins.jenkins.io/aws-parameter-store)

One-time step:
* Install the AWS Parameter Store Build Wrapper Plugin on your Jenkins master
* For cross-account deployments, create an AWS Credential with the id '&lt;ENVIRONMENT&gt;_PARAMETER_STORE_ACCESS' that provides access to ParameterStore for that account.

By default, parameters will be retrieved from the ParameterStore path constructed from your project's Git Organization, Git Repository name, and environment.  Eg: If my terraform project were at https://github.com/Manheim/fake-terraform-project, then my 'qa' environment would receive parameters from the ParameterStore path '/Manheim/fake-terraform-project/qa'.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

ParameterStoreBuildWrapperPlugin.init() // Enable ParameterStoreBuildWrapperPlugin

def validate = new TerraformValidateStage()

// Inject all parameters in /<GitOrg>/<GitRepo>/qa
def deployQA = new TerraformEnvironmentStage('qa')

// Inject all parameters in /<GitOrg>/<GitRepo>/uat
def deployUat = new TerraformEnvironmentStage('uat')

// Inject all parameters in /<GitOrg>/<GitRepo>/prod
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
