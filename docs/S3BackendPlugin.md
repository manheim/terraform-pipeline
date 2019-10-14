## [S3BackendPlugin](../src/S3BackendPlugin.groovy)

Enable this plugin to store state in S3.

One-time setup

* Have a pre-created S3 bucket to store your state in.

Terraform state can be stored in S3 with the following configuration, at minimum:

```
# main.tf

terraform {
  backend "s3" { }
}
```

See: https://www.terraform.io/docs/backends/types/s3.html

The configuration above still requires you to tell Terraform at minimum:
1. the bucket
2. the region of the bucket
3. the path to the environment-specific terraform state (an S3 Object key)

Hardcoding these values into your terraform code would prevent you from reusing the same terraform templates across all of your environments.  Terraform allows you to parameterize these values, but they're treated differently from normal terraform variables.  A separate -backend-config flag for terraform init is used to configure individual backend variables (See: https://www.terraform.io/docs/backends/config.html#partial-configuration).  These flags will automatically be added to your terraform init command, based on the following:

* Specify an S3 bucket to store your state, with `-backend-config=bucket=<value>` by setting any of the following:
    * An `S3_BACKEND_BUCKET` environment variable
    * An `${environment.toUpperCase()}_S3_BACKEND_BUCKET` environment variable
    * An `${environment}_S3_BACKEND_BUCKET` environment variable
* Specify the region of your S3 bucket, with `-backend-config=region=<value>` by setting any of the following:
    * An `S3_BACKEND_REGION` environment variable
    * An `${environment.toUpperCase()}_S3_BACKEND_REGION` environment variable
    * An `${environment}_S3_BACKEND_REGION` environment variable
* (Optional) Enable state locking and consistency checking via Dynamo DB, with `-backend-config=dynamodb_table=<value>` by setting any of the following:
    * An `S3_BACKEND_DYNAMODB_TABLE` environment
    * An `${environment.toUpperCase()}_S3_BACKEND_DYNAMODB_TABLE` environment
    * An `${environment}_S3_BACKEND_DYNAMODB_TABLE` environment
* (Optional) Enable server side encryption of the state file, with `-backend-config=encrypt=<value>` by setting any of the following:
    * An `S3_BACKEND_ENCRYPT` environment
    * An `${environment.toUpperCase()}_S3_BACKEND_ENCRYPT` environment
    * An `${environment}_S3_BACKEND_ENCRYPT` environment
* (Optional) Enable SSE-KMS of the state file, with `-backend-config=kms_key_id=<value>` by setting any of the following:
    * An `S3_BACKEND_KMS_KEY_ID` environment
    * An `${environment.toUpperCase()}_S3_BACKEND_KMS_KEY_ID` environment
    * An `${environment}_S3_BACKEND_KMS_KEY_ID` environment

Each environment state will automatically be given a unique path to an S3 object, in the form `-backend-config=key=terraform/<GitOrg>/<GitRepo>/<environment>`.  If you wish to use a different key, you can specify a custom environment-specific key by setting an `S3BackendPlugin.keyPattern` Closure. Eg:

```
S3BackendPlugin.keyPattern = { String env -> "customPatternFor/entropy/${env}" }
S3BackendPlugin.init()
```

Example pipeline using the S3BackedPlugin:

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

S3BackendPlugin.init()

def validate = new TerraformValidateStage()

// terraform init -backend-config=key=terraform/<GitOrg>/<GitRepo>/qa
def deployQA = new TerraformEnvironmentStage('qa')

// terraform init -backend-config=key=terraform/<GitOrg>/<GitRepo>/uat
def deployUat = new TerraformEnvironmentStage('uat')

// terraform init -backend-config=key=terraform/<GitOrg>/<GitRepo>/prod
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

