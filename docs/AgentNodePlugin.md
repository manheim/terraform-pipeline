## [AgentNodePlugin](../src/AgentNodePlugin.groovy)

This plugin allows you to run the terraform stages in a docker container. This **DOES NOT WORK WITH AWSSUME** you should be using iam_block with aws provider with terraform.

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

AgentNodePlugin.withAgentDockerImage('hashicorp/terraform:0.10.2')
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
