## [FileParametersPlugin](../src/FileParametersPlugin.groovy)

Enable this plugin to inject variables from a local properties file.

The properties file should have variables in the form `KEY=VALUE`, with each variable on its own line.  Values can reference other existing environment variables defined elsewhere, using the [Jenkinsfile `env` variable](https://jenkins.io/doc/book/pipeline/jenkinsfile/#using-environment-variables), and [Groovy string interpolation](http://docs.groovy-lang.org/latest/html/documentation/#_string_interpolation).  Eg: `DATABASE_URL=${env.QA_DATABASE_URL}`.

You should not store sensitive data in these properties files.  Instead, sensitive data should be stored with the [CredentialsPlugin](./src/CredentialsPlugin.groovy), [ParameterStoreBuildWrapperPlugin](#parameterstorebuildwrapperplugin) with encryption, or any other tool that supports safe storage.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

FileParametersPlugin.init() // Enable FileParametersPlugin

def validate = new TerraformValidateStage()

// Inject all parameters in qa.properties
def deployQA = new TerraformEnvironmentStage('qa')

// Inject all parameters in uat.properties
def deployUat = new TerraformEnvironmentStage('uat')

// Inject all parameters in prod.properties
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
