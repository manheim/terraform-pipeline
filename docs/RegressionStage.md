# RegressionStage

Some pipelines dealing with application code may need to run automation tests. The RegressionStage can be used to accommodate this. Create a RegressionStage then add it to your other linked stages in the appropriate position. Below is an example for running tests after a QA deployment:
```
// Jenkinsfile
...
def testQa = new RegressionStage()

validate.then(deployQa)
        .then(testQa)
...
```

By default, the RegressionStage will execute a `./bin/test.sh` script. If you wish to use a different path or script name, you can do so by passing it into the RegressionStage constructor:
```
// Jenkinsfile
...
def testQa = new RegressionStage('./some/other/location/some_other_test.sh')

validate.then(deployQa)
        .then(testQa)
...
```

If your test suite lives outside of the code, you can call `withScm()` and pass it the git URL of your automation test:
```
// Jenkinsfile
...
def testQa = new RegressionStage().withScm('git@SomeHost:SomeTeam/SomeAutomationRepository.git')
validate.then(deployQa)
        .then(testQa)
...

```

There could be instances where multiple repositories are required to execute automation tests (such as needing to clone application code as well as automation code). When calling `withScm()` more than once, the repositories become cumulative and it will check out all repositories into their respective subdirectories (named by the project name referenced by the git url). Below is an example of a scenario where the application repository and automation repository are both required. The test script to execute is also located in a subdirectory which we can use `changeDirectory()` so that we are in the appropriate directory.

```
// Jenkinsfile
...
def testQa = new RegressionStage().withScm('git@SomeHost:SomeTeam/SomeAutomationRepository.git')
                                  .withScm('git@SomeHost:SomeTeam/SomeApplicationRepository.git')
                                  .changeDirectory('SomeAutomationRespository')
validate.then(deployQa)
        .then(testQa)
...
```
The above example will check out `git@SomeHost:SomeTeam/SomeAutomationRepository.git` into a subdirectory named `SomeAutomationRepository` and `git@SomeHost:SomeTeam/SomeApplicationRepository.git` into a subdirectory named `SomeApplicationRepository`.

Note that if withScm is only called once, there will not be a subdirectory and it will check out the repository at the root.

