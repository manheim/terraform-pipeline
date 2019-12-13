## [TfvarsFilesPlugin](../src/TfvarsFilesPlugin.groovy)

This plugin allows you to add `-var-file=${environment}.tfvars` to your plan
and apply commands.  It supports being configured for a directory relative to
to the git root. 

For the following repository:

```
.
├── Jenkinsfile
├── main.tf
├── variables.tf
└── tfvars
    ├── qa.tfvars
    ├── uat.tfvars
    └── prod.tfvars
```

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.3']) _

Jenkinsfile.init(this)

TfvarsFilesPlugin.withDirectory('./tfvars').init()

def validate = new TerraformValidateStage()

def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
