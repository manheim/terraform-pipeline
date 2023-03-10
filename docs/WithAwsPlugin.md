## [WithAwsPlugin](../src/WithAwsPlugin.groovy)

Enable this plugin to manage AWS Authentication with the [pipeline-aws-plugin](https://github.com/jenkinsci/pipeline-aws-plugin).

One-time setup

* Have the [pipeline-aws-plugin] installed on your Jenkins instance.
* (Optional) Define an AWS_ROLE_ARN variable, or environment-specific `${env}_AWS_ROLE_ARN`

Example pipeline using the WithAwsPlugin using an explicit role:

```
// Jenkinsfile
@Library(['terraform-pipeline@v4.3']) _

Jenkinsfile.init(this)

WithAwsPlugin.withRole(MY_ROLE_ARN).init()

def validate = new TerraformValidateStage()

// withAws(role: MY_ROLE_ARN)
def deployQA = new TerraformEnvironmentStage('qa')

// withAws(role: MY_ROLE_ARN)
def deployUat = new TerraformEnvironmentStage('uat')

// withAws(role: MY_ROLE_ARN)
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

Example pipeline using the WithAwsPlugin using implicit roles:

```
// Jenkinsfile
@Library(['terraform-pipeline@v4.3']) _

Jenkinsfile.init(this)

WithAwsPlugin.withRole().init()

def validate = new TerraformValidateStage()

// withAws(role: AWS_ROLE_ARN) or withAws(role: QA_AWS_ROLE_ARN), where either AWS_ROLE_ARN or QA_AWS_ROLE_ARN are defined.  Nothing if neither is defined.
def deployQA = new TerraformEnvironmentStage('qa')

// withAws(role: AWS_ROLE_ARN) or withAws(role: UAT_AWS_ROLE_ARN), where either AWS_ROLE_ARN or UAT_AWS_ROLE_ARN are defined.  Nothing if neither is defined.
def deployUat = new TerraformEnvironmentStage('uat')

// withAws(role: AWS_ROLE_ARN) or withAws(role: PROD_AWS_ROLE_ARN), where either AWS_ROLE_ARN or PROD_AWS_ROLE_ARN are defined.  Nothing if neither is defined.
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

If you want to specify a role session duration other than the default of 1 hour (3600 seconds), you can do so by providing an integer duration to `withDuration()`:

```
WithAwsPlugin.withDuration(43200).init()
```

or, with a specific role ARN

```
WithAwsPlugin.withRole('MY_ROLE_ARN').withDuration(43200).init()
```
