## [ConfirmApplyPlugin](../src/ConfirmApplyPlugin.groovy)

This plugin is enabled by default.

It's a good practice to review the terraform plan before applying changes to any environment, to confirm that the changes that are being applied are the same changes that are expected.  The ConfirmApplyPlugin will pause your pipeline after the Plan step, allowing a human to review the changes.  A human must then manually Confirm the changes by clicking on the pipeline, before the changes are applied.

The pipeline will pause for a limited amount of time - 15 minutes.  Once that timeout is exceeded, that particular pipeline run will be canceled.

This functionality of this plugin can be disabled with the following configuration:

```
ConfirmApplyPlugin.disable()
