# Unreleased

* [Issue #344](https://github.com/manheim/terraform-pipeline/issues/344) Add PLAN_ONLY parameter to PlanOnlyPlugin
  * **BREAKING CHANGE** This change is a breaking change. Prior to this update, applying the PlanOnlyPlugin would restrict the pipeline to only running `terraform plan`. This update changes behavior to simply providing a `PLAN_ONLY` boolean parameter that can be set to restrict the build behavior. It defaults to `false`.
* [Issue #347](https://github.com/manheim/terraform-pipeline/pull/348) Feature: TerraformOutputOnlyPlugin - can restrict a pipeline run to displaying the current state outputs only via new job parameters.
* [Issue #318](https://github.com/manheim/terraform-pipeline/issues/318) Feature: Show environment on confirm command page
* [Issue #354](https://github.com/manheim/terraform-pipeline/issues/354) Feature: Add TerraformTaintPlugin - Allows performing `terraform taint` or `terraform untaint` prior to the plan phase.

# v5.15

* [Issue #172](https://github.com/manheim/terraform-pipeline/issues/172) Feature: ConditionalApplyPlugin - can allow apply for specific environments on all branches/PRs.
* [Issue #329](https://github.com/manheim/terraform-pipeline/issues/329) Feature: ConditionalApplyPlugin - can be disabled to allow apply on all branches/PRs.
* [Issue #320](https://github.com/manheim/terraform-pipeline/issues/320) Bug Fix: GithubPRPlugin should stop on plan errors, and display error to the user.
* [Issue #331](https://github.com/manheim/terraform-pipeline/issues/331) Bug Fix: terraform-pipeline should be usable even if Docker-Pipeline-plugin is not installed (and Docker featuers are not used)
* [Issue #335](https://github.com/manheim/terraform-pipeline/issues/335) Testing: Rename DummyJenkinsfile to MockWorkflowScript to better distinguish Jenkinsfile references.
* [Issue #271](https://github.com/manheim/terraform-pipeline/issues/271) Testing: Cleanup Test resets for any static state.  New Resettable and ResetStaticStateExtension available.
* [Issue #332](https://github.com/manheim/terraform-pipeline/issues/332) Testing: Junit 4 to 5.7, upgrade hamcrest from 1.3 to 2.2, remove junit-hierarchicalcontextrunner dependency

# v5.14

* [Issue #25](https://github.com/manheim/terraform-pipeline/issues/25) Allow 'apply' on branches other than master for some environments

# v5.13

* [Issue #321](https://github.com/manheim/terraform-pipeline/issues/321) Upgrade groovy from 2.4.11 to 2.4.12 (fix travisCi failures)
* [Issue #311](https://github.com/manheim/terraform-pipeline/issues/311) Fix non-deterministic test failures
* [Issue #316](https://github.com/manheim/terraform-pipeline/issues/316) Implement more granular decorations in TerraformEnvironmentStage.
* [Issue #250](https://github.com/manheim/terraform-pipeline/issues/250) Implement TerraformEnvironmentStageShellHookPlugin

# v5.12

* [Issue #303](https://github.com/manheim/terraform-pipeline/issues/303) Update ValidateFormatPlugin Documentation - additional options command
* [Issue #300](https://github.com/manheim/terraform-pipeline/issues/300) Trim whitespace when detecting terraform version from file.
* [Issue #299](https://github.com/manheim/terraform-pipeline/issues/299) Support for Global AWS Parameter Store

# v5.11

* [Issue #83](https://github.com/manheim/terraform-pipeline/issues/83) Add a plugin to support `terraform fmt`
* [Issue #296](https://github.com/manheim/terraform-pipeline/issues/296) Allow TagPlugin to be disabled on apply
* [Issue #293](https://github.com/manheim/terraform-pipeline/issues/293) withEnv & withGlobalEnv docs
* [Issue #175](https://github.com/manheim/terraform-pipeline/issues/175) Pass terraform plan output to apply

# v5.10

* [Issue #289](https://github.com/manheim/terraform-pipeline/issues/289) TagPlugin should work with both terraform 0.11.x and 0.12.x
* [Issue #254](https://github.com/manheim/terraform-pipeline/issues/254) Auto-convert git/ssh-urls to https with GithubPRPlanPlugin

# v5.9

* Docs
  * [Issue #268](https://github.com/manheim/terraform-pipeline/issues/268) Fix broken README links
  * [Issue #275](https://github.com/manheim/terraform-pipeline/issues/275) Alphabetize Plugins in the README
* Terraform Workflow
  * [Issue #88](https://github.com/manheim/terraform-pipeline/issues/88) Add an optional DestroyPlugin to perform "terraform destroy"
  * [Issue #261](https://github.com/manheim/terraform-pipeline/issues/261) Require the user to type a confirmation to use the DestroyPlugin
  * [Issue #256](https://github.com/manheim/terraform-pipeline/issues/256) Make it easy to apply "standard tags"
  * [Issue #162](https://github.com/manheim/terraform-pipeline/issues/162) Support "plan only" on master
* terraform-pipeline enhancements
  * [Issue #272](https://github.com/manheim/terraform-pipeline/issues/272) Create a new BuildWithParametersPlugin
  * [Issue #257](https://github.com/manheim/terraform-pipeline/issues/257) Fix codecov reporting
  * [Issue #265](https://github.com/manheim/terraform-pipeline/issues/265) Make code coverage more visible - add codecov badge to README
  * [Issue #24](https://github.com/manheim/terraform-pipeline/issues/24) ConfirmApplyPlugin - allow customization

# v5.8

* Docs
  * [Issue #225](https://github.com/manheim/terraform-pipeline/issues/225) Simplify Contributor model - create PullRequests against master
  * [Issue #206](https://github.com/manheim/terraform-pipeline/issues/206) Explain the importance of plugin order
  * [Issue #219](https://github.com/manheim/terraform-pipeline/issues/219) Declarative Pipeline, Fix documentation - example code does not work
* Docker Agents
  * [Issue #230](https://github.com/manheim/terraform-pipeline/issues/230) AgentNode Support with PR Plugin - allow Dockerfile agents
* GithubPullRequests
  * [Issue #193](https://github.com/manheim/terraform-pipeline/issues/193) Support passing branch plans to Github PRs
  * [Issue #232](https://github.com/manheim/terraform-pipeline/issues/232) GithubPRPlanPlugin breaks with TerraformDirectoryPlugin
  * [Issue #234](https://github.com/manheim/terraform-pipeline/issues/234) Rename GithubPRPlanPlugin to be consistent
  * [Issue #244](https://github.com/manheim/terraform-pipeline/issues/244) Simplify GithubPRPlanPlugin configuration - auto construct github URL and slug
* Declarative Pipelines
  * [Issue #218](https://github.com/manheim/terraform-pipeline/issues/218) Preserve stashes in default declarative pipeline templates
* Terraform
  * [Issue #102](https://github.com/manheim/terraform-pipeline/issues/102) Support target when running terraform plan and apply

# v5.7

* [Issue #210](https://github.com/manheim/terraform-pipeline/issues/210) Support Terraform Landscape Plugin - Terraform Plan reformatting
* [Issue #221](https://github.com/manheim/terraform-pipeline/issues/221) Support suffixes for terraform commands
* [Issue #222](https://github.com/manheim/terraform-pipeline/issues/222) TerraformInitCommand - withPrefix doesn't support multiple prefixes

# v5.6

* [Issue #203](https://github.com/manheim/terraform-pipeline/issues/201) ParameterStoreBuildWrapperPlugin - allow path to be customized
* [Issue #192](https://github.com/manheim/terraform-pipeline/issues/192) Apply CredentialsPlugins to all Stages, not just BuildStage

# v5.5

* [Issue #151](https://github.com/manheim/terraform-pipeline/issues/151) Fix CPS mismatch errors, reduce meta magic in Jenkinsfile.groovy and Stage decorations
* [Issue #194](https://github.com/manheim/terraform-pipeline/issues/194) Add support global tfvars files.
* [Issue #198](https://github.com/manheim/terraform-pipeline/issues/198) Fix using only TerraformEnvironmentStages
* [Issue #196](https://github.com/manheim/terraform-pipeline/issues/196) Fix defect - Excessive nested closures in Jenkinsfile.init.  Drop support for deprecated `Jenkinsfile.init(this,env)`

# v5.4

* [Issue #87](https://github.com/manheim/terraform-pipeline/issues/87) Add travis CI support to the project
* [Issue #186](https://github.com/manheim/terraform-pipeline/issues/186) Add convenience method to detect the version of terraform
* [Issue #185](https://github.com/manheim/terraform-pipeline/issues/185) Fix defect: filename parameter is not used in TerraformPlugin.readFile

# v5.3

* [Issue #178](https://github.com/manheim/terraform-pipeline/issues/178) Fix broken link to source code in AwssumePlugin  docs
* Added the [TfvarsFilesPlugin](./docs/TfvarsFilesPlugin.md) to support adding var files to plan and apply commands.
* [Issue #109](https://github.com/manheim/terraform-pipeline/issues/109) Support Terraform 0.12

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
