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
         .withTag('repo', Jenkinsfile.instance.getScmUrl()) // Dynamic tags from your git configuration
         .withEnvironmentTag('environment') // Dynamic tags from TerraformEnvironmentStage
         .withTagFromEnvironmentVariable('team', 'TEAM') // Dynamic tags from an environment variable
         .withTagFromFile('changeId', 'change-id.txt') // Dynamic tags from file
         .init()

def validate = new TerraformValidateStage()

// -var='tags={"simple":"sometag","repo":"<repo>","environment":"qa","team":"<team>","changeId":"<changeId>"}'
def deployQa = new TerraformEnvironmentStage('qa')
// -var='tags={"simple":"sometag","repo":"<repo>","environment":"uat","team":"<team>","changeId":"<changeId>"}'
def deployUat = new TerraformEnvironmentStage('uat')
// -var='tags={"simple":"sometag","repo":"<repo>","environment":"prod","team":"<team>","changeId":"<changeId>"}'
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
         .withTag('repo', Jenkinsfile.instance.getScmUrl()) // Dynamic tags from your git configuration
         .withEnvironmentTag('environment') // Dynamic tags from TerraformEnvironmentStage
         .withTagFromEnvironmentVariable('team', 'TEAM') // Dynamic tags from an environment variable
         .withTagFromFile('changeId', 'change-id.txt') // Dynamic tags from file
         .init()

def validate = new TerraformValidateStage()

// -var='myCustomTags={"simple":"sometag","repo":"<repo>","environment":"qa","team":"<team>","changeId":"<changeId>"}'
def deployQa = new TerraformEnvironmentStage('qa')
// -var='myCustomTags={"simple":"sometag","repo":"<repo>","environment":"uat","team":"<team>","changeId":"<changeId>"}'
def deployUat = new TerraformEnvironmentStage('uat')
// -var='myCustomTags={"simple":"sometag","repo":"<repo>","environment":"prod","team":"<team>","changeId":"<changeId>"}'
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```


If using a plan file, you can disable passing variables on the CLI for that command.

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this, env)
TagPlugin.withTag('simple', 'sometag') // Simple static tags
         .withTag('repo', Jenkinsfile.instance.getScmUrl()) // Dynamic tags from your git configuration
         .withEnvironmentTag('environment') // Dynamic tags from TerraformEnvironmentStage
         .withTagFromEnvironmentVariable('team', 'TEAM') // Dynamic tags from an environment variable
         .withTagFromFile('changeId', 'change-id.txt') // Dynamic tags from file
         .disableOnApply()
         .init()

def validate = new TerraformValidateStage()

// Tag variables only passsed on plan, and skipped on apply
// -var='tags={"simple":"sometag","repo":"<repo>","environment":"qa","team":"<team>","changeId":"<changeId>"}'
def deployQa = new TerraformEnvironmentStage('qa')
// -var='tags={"simple":"sometag","repo":"<repo>","environment":"uat","team":"<team>","changeId":"<changeId>"}'
def deployUat = new TerraformEnvironmentStage('uat')
// -var='tags={"simple":"sometag","repo":"<repo>","environment":"prod","team":"<team>","changeId":"<changeId>"}'
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

If you want to simplify the cli command you can pass the tags using `-var-file` instaed.

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this, env)
TagPlugin.withTag('simple', 'sometag') // Simple static tags
         .withTag('repo', Jenkinsfile.instance.getScmUrl()) // Dynamic tags from your git configuration
         .withEnvironmentTag('environment') // Dynamic tags from TerraformEnvironmentStage
         .withTagFromEnvironmentVariable('team', 'TEAM') // Dynamic tags from an environment variable
         .withTagFromFile('changeId', 'change-id.txt') // Dynamic tags from file
         .writeToFile()
         .init()

def validate = new TerraformValidateStage()

// Tag variables will be passsed via `-var-file`
// -var-file=./qa-tags.tfvars
def deployQa = new TerraformEnvironmentStage('qa')
// -var-file=./uat-tags.tfvars
def deployUat = new TerraformEnvironmentStage('uat')
// -var-file=./prod-tags.tfvars
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
