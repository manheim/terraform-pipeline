## [DefaultEnvironmentPlugin](../src/DefaultEnvironmentPlugin.groovy)

This plugin is enabled by default.

Provides a terraform variable `environment` to all TerraformEnvironmentStages by default, by setting an environment variable in the form `TF_VAR_environment`.  The value of `environment` is the same as the environment value that you passed when creating an instance of the TerraformEnvironmentStage.

