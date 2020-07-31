## [TagPlugin](../src/TagPlugin.groovy)

Enable this plugin to inject tags into your terraform project using terraform CLI variables.

Your terraform project will need to accept external tags as a map. For example:

```
# variables.tf
...
variable "tags" {
  type = map
  default = { }
}
```

Modify your pipeline to enable the `TagPlugin`, and configure the tags that you want to pass to your terraform code.  Those tags will translated to a terraform `-var` argument when `terraform plan` and `terraform apply` are run.

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this, env)
TagPlugin.withTag('simple', 'sometag') // Simple static tags
         .withTag('project', Jenkinsfile.getRepoName()) // Dynamic tags from your git configuration
         .withTag('repo', Jenkinsfile.getScmUrl()) // Dynamic tags from your git configuration
         .withTag('team', '$TEAM') // Dynamic tags from an environment variable
         .withEnvironmentTag('environment') // Dynamic tags from TerraformEnvironmentStage
         .init()

def validate = new TerraformValidateStage()

// -var='tags={"simple":"sometag","project":"<project>","repo":"<repo>","team":"<team>","environment":"qa"}'
def deployQa = new TerraformEnvironmentStage('qa')
// -var='tags={"simple":"sometag","project":"<project>","repo":"<repo>","team":"<team>","environment":"uat"}'
def deployUat = new TerraformEnvironmentStage('uat')
// -var='tags={"simple":"sometag","project":"<project>","repo":"<repo>","team":"<team>","environment":"prod"}'
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

If needed, you can alter the name of the terraform variable used to pass your tags.

```
# variables.tf
...
variable "myCustomTags" {
  type = map
  default = { }
}
```

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this, env)
TagPlugin.withVariableName('myCustomTags')
         .withTag('simple', 'sometag') // Simple static tags
         .withTag('project', Jenkinsfile.getRepoName()) // Dynamic tags from your git configuration
         .withTag('repo', Jenkinsfile.getScmUrl()) // Dynamic tags from your git configuration
         .withTag('team', '$TEAM') // Dynamic tags from an environment variable
         .withEnvironmentTag('environment') // Dynamic tags from TerraformEnvironmentStage
         .init()

def validate = new TerraformValidateStage()

// -var='myCustomTags={"simple":"sometag","project":"<project>","repo":"<repo>","team":"<team>","environment":"qa"}'
def deployQa = new TerraformEnvironmentStage('qa')
// -var='myCustomTags={"simple":"sometag","project":"<project>","repo":"<repo>","team":"<team>","environment":"uat"}'
def deployUat = new TerraformEnvironmentStage('uat')
// -var='myCustomTags={"simple":"sometag","project":"<project>","repo":"<repo>","team":"<team>","environment":"prod"}'
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```


