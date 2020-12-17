## [TerraformEnvironmentStageShellHookPlugin](../src/TerraformEnvironmentStageShellHookPlugin.groovy)

This plugin allows inserting shell script hooks at various points in the TerraformEnvironmentStage execution. Please see the [TerraformEnvironmentStage source code](../src/TerraformEnvironmentStage.groovy) for a list of the supported hook points. At this time, they are:

* ``TerraformEnvironmentStage.ALL``
* ``TerraformEnvironmentStage.INIT_COMMAND``
* ``TerraformEnvironmentStage.PLAN``
* ``TerraformEnvironmentStage.PLAN_COMMAND``
* ``TerraformEnvironmentStage.APPLY``
* ``TerraformEnvironmentStage.APPLY_COMMAND``

Each hook point "wraps" various parts of the Stage, and supports a total of four hooks:

* Before the wrapped code runs (``WhenToRun.BEFORE``)
* **The default,** after the wrapped code runs successfully (``WhenToRun.ON_SUCCESS``)
* After the wrapped code runs and fails (``WhenToRun.ON_FAILURE``)
* After the wrapped code runs, regardless of success or failure (``WhenToRun.AFTER``)

```
// Jenkinsfile
@Library(['terraform-pipeline@v']) _

import static TerraformEnvironmentStageShellHookPlugin.WhenToRun

Jenkinsfile.init(this)

TerraformEnvironmentStageShellHookPlugin.withHook(TerraformEnvironmentStage.APPLY, './bin/after_successful_apply.sh')
                                        .withHook(TerraformEnvironmentStage.INIT, './bin/download_deps.sh', WhenToRun.BEFORE)
                                        .withHook(TerraformEnvironmentStage.ALL, './bin/cleanup.sh', WhenToRun.AFTER)
                                        .init()

def validate = new TerraformValidateStage()

def deployQA = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQA)
        .then(deployUat)
        .then(deployProd)
        .build()
```
