## [AgentNodePlugin](../src/AgentNodePlugin.groovy)

This plugin allows you to run the terraform stages in a docker container. This **DOES NOT WORK WITH AWSSUME** you should be using iam_block with aws provider with terraform.

### Using pre-build docker image
```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

AgentNodePlugin.withAgentDockerImage("hashicorp/terraform:latest")
               .withAgentDockerImageOptions("--entrypoint=''")
               .init()

def validate = new TerraformValidateStage()
def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```

### Using Dockerfile in repo
```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

AgentNodePlugin.withAgentDockerImage('custom/terraform-ruby:latest')
               .withAgentDockerImageOptions("--entrypoint=''")
               .withAgentDockerfile()
               .init()

def validate = new TerraformValidateStage()
def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```

### Optionally use a Dockerfile with a different filename
```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

AgentNodePlugin.withAgentDockerImage('custom/terraform-ruby:latest')
               .withAgentDockerImageOptions("--entrypoint=''")
               .withAgentDockerfile('MyDockerfile')
               .init()

def validate = new TerraformValidateStage()
def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```

