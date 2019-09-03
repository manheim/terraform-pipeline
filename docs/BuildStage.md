# BuildStage

Some pipelines dealing with application code may to build deployment artifacts. The BuildStage can be used to accommodate this. Create a BuildStage then add it to your other linked stages in the appropriate position. Below is an example for building a deployment artifact which is later used to deploy to QA.

Let terraform-pipline know which build artifacts to save and make available using the `saveArtifact` method.  Artifacts that match the pattern passed to `saveArtifact` will automatically be stashed after BuildStage, and unstashed in each subsequent TerraformEnvironmentStage.

```
// Jenkinsfile
...
def build = new BuildStage().saveArtifact('*/target/MyApp.war')

validate.then(build)
        .then(deployQa) // MyApp.war will be available in the working directory
...
```
