## [CredentialsPlugin](../src/CredentialsPlugin.groovy)

Enable this plugin to inject credentials into your stages using the [Jenkins Credentials Plugin](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin).

One-time setup:
* Install the [Jenkins Credentials Plugin](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin) on your Jenkins master.
* Define a credential that you want to inject.  Currently, only usernamePassword credentials are supported.

Specify the credential that you want to inject in your stages.  Optionally provide custom username/password environment variables that will contain the credential values for your use.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

// MY_CREDENTIALS_USERNAME and MY_CREDENTIALS_PASSWORD will contain the respective username/password values of the 'my-credentials' credential.
CredentialsPlugin.withBuildCredentials('my-credentials').init()

def validate = new TerraformValidateStage()
def build = new BuildStage()
def deployQa = new TerraformEnvironmentStage('qa')
def testQa = new RegressionStage()
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(build)
        .then(deployQa)
        .then(testQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```
