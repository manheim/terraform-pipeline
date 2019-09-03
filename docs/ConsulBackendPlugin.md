## [ConsulBackendPlugin](../src/ConsulBackendPlugin.groovy)

Enable this plugin to store state in Consul.

Terraform state can be stored in consul with the following configuration:

```
# main.tf

terraform {
  backend "consul" { }
}
```

See: https://www.terraform.io/docs/backends/types/consul.html

The configuration above still requires you to tell Consul the path where environment states should be managed, but hardcoding that value into your terraform template prevents you from reusing the same template across all your environments.  Ideally, you would provide a variable for the path for each environment, but terraform treats backend configuration as special, and you can't use normal variables.  Instead terraform provides a separate `-backend-config` flag for `terraform init` to configure different backends (See: https://www.terraform.io/docs/backends/config.html#partial-configuration).

By enabling this plugin, each environment state will automatically be given a unique consul path to store state, in the form `-backend-config=path=terraform/<GitOrg>/<GitRepo>/<environment>`.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

ConsulBackendPlugin.init()

def validate = new TerraformValidateStage()

// terraform init -backend-config=path=/<GitOrg>/<GitRepo>/qa
def deployQa = new TerraformEnvironmentStage('qa')

// terraform init -backend-config=path=/<GitOrg>/<GitRepo>/uat
def deployUat = new TerraformEnvironmentStage('uat')

// terraform init -backend-config=path=/<GitOrg>/<GitRepo>/prod
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
