## [TerraformEnvironmentStage](../src/TerraformEnvironmentStage.groovy)

### Pass in Global Environment variable

You can pass in global environment variables too all stages in your pipeline using `withGlobalEnv`

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

TerraformEnvironmentStage.withGlobalEnv('KEY_01', 'VALUE_01')
                         .withGlobalEnv('KEY_01', 'VALUE_02')

def validate = new TerraformValidateStage()
def deployQA = new TerraformEnvironmentStage('qa')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployProd)
        .build()
```

### Pass in Stage Specific Environment variable

You can pass in stage specific environment variables each stage in your pipeline using `withEnv`

```
// Jenkinsfile
@Library(['terraform-pipeline']) _

Jenkinsfile.init(this)

def validate = new TerraformValidateStage()
def deployQA = new TerraformEnvironmentStage('qa').withEnv('KEY_01', 'VALUE_01')
                                                  .withEnv('KEY_02', 'VALUE_02')
def deployProd = new TerraformEnvironmentStage('prod').withEnv('KEY_03', 'VALUE_03')
                                                      .withEnv('KEY_04', 'VALUE_04')

validate.then(deployQA)
        .then(deployProd)
        .build()
```