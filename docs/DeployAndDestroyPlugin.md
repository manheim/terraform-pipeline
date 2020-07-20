## [DesployAndDestroyPlugin](../src/DesployAndDestroyPlugin.groovy)

Add a `terraform destroy` stage after deployment. (Requires manual confirmation)

When this plugin is enabled, the pipeline will follow these steps:  
1. Run a `terraform plan`
2. Ask for human confirmation to proceed with the apply.
3. Run the `terraform apply` command.
4. Run a `terraform plan -destroy` to see which resources will be destroyed.
5. Ask for human confirmation to proceed with the destroy.
6. Run `terraform destroy` to destroy the resources.


```
// Jenkinsfile
@Library(['terraform-pipeline@v3.10']) _

Jenkinsfile.init(this, env)

// This adds the destroy functionality at the end of the pipeline
DeployAndDestroyPlugin.init()

def validate = new TerraformValidateStage()

def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

When using this plugin, your pipeline will look something like this:

![DestroyPlugin pipeline](../images/deploy-and-destroy-pipeline.png)