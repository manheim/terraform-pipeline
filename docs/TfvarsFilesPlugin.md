## [TfvarsFilesPlugin](../src/TfvarsFilesPlugin.groovy)

This plugin allows you to add `-var-file=${environment}.tfvars` to your plan
and apply commands.  It supports being configured for a directory relative to
to the git root and global files being applied to every plan and apply command.

For the following repository:

```
.
├── Jenkinsfile
├── main.tf
├── variables.tf
└── tfvars
    ├── qa.tfvars
    ├── uat.tfvars
    ├── prod.tfvars
    └── global.tfvars
```
This Jenkinsfile setup will include a `-var-file` argument for both
`qa.tfvars` and `global.tfvars` on the QA plan and apply commands.  Similarly,
the UAT commands will include `uat.tfvars` and `global.tfvars`.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.3']) _

Jenkinsfile.init(this)

TfvarsFilesPlugin.withDirectory('./tfvars')
                 .withGlobalVarFile('global.tfvars')
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
