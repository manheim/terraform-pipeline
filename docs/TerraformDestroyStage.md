# TerraformDestroyStage

This stage allows desctruction of infrastructure.  

The TerraformDestroyStage consists of 3 parts:
1. Run a `terraform plan -destroy` to see what resources would be destroyed.
2. Wait for human confirmation to proceeed with the destroy.
3. Run `terraform destroy` for the specified environment.

```
// Jenkinsfile
...

def destroyQa = new TerraformDestroyStage('qa')
def destroyUat = new TerraformDestroyStage('uat')
def destroyProd = new TerraformDestroyStage('prod')

destroyQa.then(deployUat)
         .then(deployProd)
         .build()
...
```