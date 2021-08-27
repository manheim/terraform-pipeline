## [CredentialsPlugin](../src/CredentialsPlugin.groovy)

Enable this plugin to inject credentials into your stages using the [Jenkins Credentials Plugin](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin).

One-time setup:
* Install the [Jenkins Credentials Binding Plugin](https://www.jenkins.io/doc/pipeline/steps/credentials-binding/) on your Jenkins master.
* Define a credential that you want to inject.

Add any number of credentials bindings that you want to wrap your stages, with `withBinding`.  Each call to this method cumulatively add more credentials.  See the [Credentials Binding Plugin homepage](https://www.jenkins.io/doc/pipeline/steps/credentials-binding/) for the list of supported bindings.

```
// Jenkinsfile
@Library(['terraform-pipeline@v5.0']) _

Jenkinsfile.init(this)

// Add credentials to all Stages from usernamePassword, usernameColonPassowrd, and string credentials
CredentialsPlugin.withBinding { usernamePassword(credentialsId: 'my-user-pass', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD') }
                 .withBinding { usernameColonPassword(credentialsId: 'my-user-colon-pass', variable: 'USERPASS') }
		 .withBinding { string(credentialsId: 'my-string-token', variable: 'TOKEN') }
		 .init()

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
