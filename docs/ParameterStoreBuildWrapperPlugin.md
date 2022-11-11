## [ParameterStoreBuildWrapperPlugin](../src/ParameterStoreBuildWrapperPlugin.groovy)

Enable this plugin to inject variables using the [AWS Parameter Store Build Wrapper Plugin](https://plugins.jenkins.io/aws-parameter-store)

One-time step:
* Install the AWS Parameter Store Build Wrapper Plugin on your Jenkins server
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

Optionally, you can override the default ParameterStore path with your own custom pattern.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

// Enable ParameterStoreBuildWrapperPlugin with a custom path pattern
ParameterStoreBuildWrapperPlugin.withPathPattern { options -> "/${options['organization']}/${options['environment']}/${options['repoName']}" }
                                .init()

def validate = new TerraformValidateStage()

// Inject all parameters in /<GitOrg>/qa/<GitRepo>
def deployQA = new TerraformEnvironmentStage('qa')

// Inject all parameters in /<GitOrg>/uat/<GitRepo>
def deployUat = new TerraformEnvironmentStage('uat')

// Inject all parameters in /<GitOrg>/prod/<GitRepo>
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```


You can also optionally support Global Parameters applied to the following stages `VALIDATE`, `PLAN`, `APPLY`


```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

ParameterStoreBuildWrapperPlugin.withGlobalParameter('/somePath/') // get all keys under `/somePath/`
                .withGlobalParameter('/someOtherPath/', [naming: 'relative', recursive: true]) // get all keys recursively under `/someOtherPath/`
                .init() // Enable ParameterStoreBuildWrapperPlugin

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