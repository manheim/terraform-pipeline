## [DestroyPlugin](../src/DestroyPlugin.groovy)

Enable this plugin to use `terraform destroy` to destroy your environment(s).

When this plugin is enabled, the pipeline will follow these steps:  
1. Run a `terraform plan -destroy` to display which resources will get destroyed.
2. Ask for human confirmation to proceed with the destroy.
3. Run the `terraform destroy` command.


```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// This enables the destroy functionality
DestroyPlugin.init()

def validate = new TerraformValidateStage()

def destroyQa = new TerraformEnvironmentStage('qa')
def destroyUat = new TerraformEnvironmentStage('uat')
def destroyProd = new TerraformEnvironmentStage('prod')

validate.then(destroyQa)
        .then(destroyUat)
        .then(destroyProd)
        .build()
```

When using this plugin, your pipeline will look something like this:

![DestroyPlugin pipeline](../images/destroy-pipeline.png)