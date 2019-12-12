# Unreleased

* Added the [TfvarsFilesPlugin](./docs/TfvarsFilesPlugin.md) to support adding var files to plan and apply commands.

# v5.2

* [Issue #168](https://github.com/manheim/terraform-pipeline/issues/168) Fix typo in Jenkinsfile
* [Issue #171](https://github.com/manheim/terraform-pipeline/issues/171) Fix gradlew, so unit tests can run

# v5.1

* [Issue #156](https://github.com/manheim/terraform-pipeline/issues/156) Remove and ignore the build directory
* [Issue #160](https://github.com/manheim/terraform-pipeline/issues/160) S3BackendPlugin - allow encryption of S3 state

# v5.0

* [Issue #132](https://github.com/manheim/terraform-pipeline/issues/132) OpenSource this project.
* [Issue #148](https://github.com/manheim/terraform-pipeline/issues/148) Remove `env` as an argument to `Jenkinsfile.init`, it's not needed.
* [Issue #42](https://github.com/manheim/terraform-pipeline/issues/42) Enable Declarative pipelines, which in turn, enables Restart From Stage.

# v4.3

* [Issue #107](https://github.com/manheim/terraform-pipeline/issues/107) WithAwsPlugin - alternative to AwssumePlugin to assume roles in different accounts
* [Issue #106](https://github.com/manheim/terraform-pipeline/issues/106) Use `withGlobalEnv` method on TerraformEnvironmentStage to add global variables to Stages
* [Issue #143](https://github.com/manheim/terraform-pipeline/issues/143) Fix defect introduced by v4.1, with change to CrqPlugin.
* [Issue #142](https://github.com/manheim/terraform-pipeline/issues/142) `withEnv` should be executed in order, along with every other plugin.  This was not previously the case, so environment variables were not always visible to plugins that might need it.

# v4.2
* [Issue #43](https://github.com/manheim/terraform-pipeline/issues/43) AgentNodePlugin - intial support for docker containers in TerraformValidate & TerraformEnvironmentStage

# v4.1
* [Issue #111](https://github.com/manheim/terraform-pipeline/issues/111) Improve cross-pipeline configuration for CRQPlugin
    * Allow multiple environment variables to configure CRQ_ENVIRONMENT
* [Issue #110](https://github.com/manheim/terraform-pipeline/issues/110) Improve cross-pipeline configuration for AwssumePlugin
    * Allow case-insensitive `<environment>_AWS_ROLE_ARN` variables
    * Allow hierarchy for `AWS_ROLE_ARN` variables

# v4.0
* [Issue #120](https://github.com/manheim/terraform-pipeline/issues/120) Delay when plugins are applied
    * This is a change in the plugin behavior.  Plugins will now affect TerraformCommands even after they've been instantiated - this was not previously the case.
    * The purpose of this, is to allow other Plugins to modify Environment variables.  Some TerraformCommands may be affected by the Environment variables at the time in which plugins are applied (eg: S3BackendPlugin).  Make sure that all possible environment variables are available at the time in which plugins are applied to the commands.
* [Issue #115](https://github.com/manheim/terraform-pipeline/issues/115) Expand configuration for S3BackendPlugin
    * Support upper or lower case environment prefixes
    * Support general, or environment-specific configuration
    * Standardize configuration for region, bucket, dynamodb_table
    * ACTION: *Deprecated* DEFAULT_S3_BACKEND_REGION - use S3_BACKEND_REGION, env_S3_BACKEND_REGION, AWS_REGION, or AWS_DEFAULT_REGION
    * ACTION: *Deprecated* env_S3_BACKEND_DYNAMO_TABLE_LOCK - use S3_BACKEND_DYNAMODB_TABLE, or env_S3_BACKEND_DYNAMODB_TABLE

# v3.12
* [Issue #24](https://github.com/manheim/terraform-pipeline/issues/24) ConfirmApplyPlugin - skip human confirmation

# v3.11
* [Issue #73](https://github.com/manheim/terraform-pipeline/issues/73) In a single-branch pipeline, allow terraform apply to happen
* Allow custom key pattern in S3BackendPlugin - https://github.com/manheim/terraform-pipeline/pull/117
* [Issue #114](https://github.com/manheim/terraform-pipeline/issues/114) Point out DefaultEnvironmentPlugin earlier in README

# v3.10
* [Issue #103](https://github.com/manheim/terraform-pipeline/issues/103) Support multiple repos
* [Issue #101](https://github.com/manheim/terraform-pipeline/issues/101) Expect test suite to exist locally by default

# v3.9
* [Issue #99](https://github.com/manheim/terraform-pipeline/issues/99) Added RegressionStage to allow running automated tests.

# v3.8
* [Issue #90](https://github.com/manheim/terraform-pipeline/issues/90) Allow configuration of the directory where Terraform runs

# v3.7
* [Issue #67](https://github.com/manheim/terraform-pipeline/issues/67) Support injecting CredentialsPlugin credentials into the BuildStage
* [Issue #71](https://github.com/manheim/terraform-pipeline/issues/71) Update README code examples with the latest stable release.
* [Issue #80](https://github.com/manheim/terraform-pipeline/issues/80) Fix S3Backend code examples.
* [Issue #72](https://github.com/manheim/terraform-pipeline/issues/72) Improve README instructions for setting up a pipeline.

# v3.6
* [Issue #84](https://github.com/manheim/terraform-pipeline/issues/84) Support DynamoDb table locking in S3BackendPlugin

# v3.5
* [Issue #75](https://github.com/manheim/terraform-pipeline/issues/75) Be able to skip awssume if not doing a cross-account deploy.  Disable by not specifying `<environment>_AWS_ROLE_ARN` variable.
* [Issue #62](https://github.com/manheim/terraform-pipeline/issues/62) Create a new FileParametersPlugin, to set environment variables through properties files.
* [Issue #76](https://github.com/manheim/terraform-pipeline/issues/76) The README shows an example of providing a local Customizations class.  That doesn't actually work.  Remove that from the README until we can find something that works.

# v3.4
* [Issue #49](https://github.com/manheim/terraform-pipeline/issues/49) Allow Consul address to be provided in ways other than hard-coding.
* [Issue #50](https://github.com/manheim/terraform-pipeline/issues/50) Allow Consul path pattern for environments to be overridden.

# v3.3
* [Issue #44](https://github.com/manheim/terraform-pipeline/issues/44) Create a new DefaultEnvironmentPlugin, to provide a terraform variable `environment` by default.

# v3.2
* [Issue #33](https://github.com/manheim/terraform-pipeline/issues/33) Add information in the README to control the node label for your pipelines.
* Update README code examples

# v3.1
* TerraformCommand Refactor: use explicit interfaces to customize commands, stop using Closures.
* Add a unit test suite for every plugin
* Add new S3BackendPlugin to support storing terraform state in S3

# v3.0
* [Issue #35](https://github.com/manheim/terraform-pipeline/issues/35): turn hardcoded backend path for terraform init into a new ConsulBackendPlugin.  BackendPath is no longer set by default.

# v2.4
* Bug fix: Prefer Jenkinsfile.defaultNodeName over environment variable DEFAULT_NODE_NAME when choosing the node to run jobs
* ParameterStoreBuildWrapperPlugin now available, and is the preferred method to retrieve ParameterStore parameters.  Start deprecating use of ParameterStoreExecPlugin.

# v2.3.1
* Optionally stash and unstash an artifact generated by the BuildStage.  See Issue #40 (https://github.com/manheim/terraform-pipeline/issues/40)
* Delete and checkout SCM only once, at the beginning of every TerraformEnvironmentStage

# v2.3
* Add a BuildStage.groovy that can be used to insert an arbitrary build script in your pipeline.

# v2.2.1
* Fix typo in README for ParameterStoreExecPlugin.  Example code did not work if you copy-paste.

# v2.2
* Update README with clarifications on Plugin use.
* Add missing AwssumePlugin to README

# v2.1
* Update README with strategies for DRY'ing up plugin configuration.

# v2.0
* [Issue #10](https://github.com/manheim/terraform-pipeline/issues/10) - flesh out a general pattern for plugins.  These are breaking changes.
* AnsiColorPlugin is no longer active by default.  Enable AnsiColorPlugin by calling `AnsiColorPlugin.init()`
* CrqPlugin is no longer active by default.  Enable CrqPlugin by calling `CrqPlugin.init()`
* Combine and rename ParameterStorePlugin to ParameterStoreExecPlugin.
* ParameterStoreExecPlugin is no longer active by default.  Enable ParameterExecStorePlugin by calling `ParameterStoreExecPlugin.init()`
* New TerraformEnvironmentStagePlugin interface - AnsiColorPlugin and CrqPlugin implements this.  Plugins should implement this type of interface going forward.
* StageConditionPlugin is now implemented as a TerraformEnvironmentStagePlugin.  It continues to be enabled by default, and continues to default to the master branch.
* Rename StageConditionPlugin to ConditionalApplyPlugin.
* TerraformConfirmPlugin is now implemented as a TerraformEnvironmentStagePlugin.  It continues to be enabled by default, with all existing defaults.
* Rename TerraformConfirmPlugin to ConfirmApplyPlugin

# v1.7
* [Issue #16](https://github.com/manheim/terraform-pipeline/issues/16): support either HTTPS or SSH urls when constructing repo slug (used for consul state path on terraform plan and terraform apply)
* Add gradle and unit testing

# v1.6
* awssume is no longer used by default in terraform-plan and terraform-apply
* A new AwssumePlugin is available, which will add awssume functionality if you want it

# v1.5
* parameter-store-exec is no longer used by default in terraform-plan, terraform-apply
* A new ParameterStoreExec Plugin is available, which will restore that functionality if you want it.
* Reduce boiler plate code for Jenkinsfile - a new init() method takes in the Script, the environment, and an optional customization class (just needs an init()) method, where you can load any plugins that might customize the behavior of your pipeline (eg: ParameterStoreExec)

# v1.3
* Store the `env` variable for later use
* Allow pipeline users to provide a DEFAULT_NODE_NAME environment variable, rather than explicitly specifying it in the pipeline
