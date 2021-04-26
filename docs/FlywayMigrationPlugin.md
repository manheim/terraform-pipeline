## [FlywayMigrationPlugin](../src/FlywayMigrationPlugin.groovy)

Enable this plugin to run automated database migrations with [Flyway](https://flywaydb.org/).  Flyway can be configured through standard configuration files, or through environment variables.  Since migrations maybe less frequent than terrform changes, by default, if a pending migrtion is detected, the pipeline will prompt you *again* to confirm that you want to proceed with the migration.

```
@Library(['terraform-pipeline']) _
Jenkinsfile.init(this, Customizations)
FlywayMigrationPlugin.init()

def validate = new TerraformValidateStage()
// For each environment
//     Run the `flyway info` command after a successful `terraform plan`
//     Run the `flyway migrate` command after a successful `terraform apply`
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

If needed, flyway configuration can be set through the FlywayCommand.

```
@Library(['terraform-pipeline']) _
Jenkinsfile.init(this, Customizations)
// Use the FlywayCommand to modify specific options like `user`, `password`, `locations`, and `url`
FlywayCommand.withUser("\$TF_VAR_USER")
             .withPassword("\$TF_VAR_PASSWORD")
             .withLocations("filesystem:`pwd`/migrations")
             .withUrl("`terraform output jdbc_url`")
FlywayMigrationPlugin.init()

def validate = new TerraformValidateStage()
// For each environment
//     Run the `flyway info` command after a successful `terraform plan`
//     Run the `flyway migrate` command after a successful `terraform apply`
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

Or through existing environment variables and the FlywayMigrationPlugin.

```
@Library(['terraform-pipeline']) _
Jenkinsfile.init(this, Customizations)
// Allow flyway to be configured through standard environment variables
FlywayMigrationPlugin.withMappedEnvironmentVariable('TF_VAR_USER', 'FLYWAY_USER')
                     .withMappedEnvironmentVariable('TF_VAR_PASSWORD', 'FLYWAY_PASSWORD')
                     .withMappedEnvironmentVariable('JDBC_URL', 'FLYWAY_URL')
                     .withMappedEnvironmentVariable('LOCATIONS', 'FLYWAY_LOCATIONS')
                     .init()

def validate = new TerraformValidateStage()
// For each environment
//     Run the `flyway info` command after a successful `terraform plan`
//     Run the `flyway migrate` command after a successful `terraform apply`
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')
def deployProd = new TerraformEnvironmentStage('prod')

validate.then(deployQa)
        .then(deployUat)
        .then(deployProd)
        .build()
```

If you don't want to be prompted a second time when migrations are detected, you can disable the migration confirmation with `FlywayMigrationPlugin.confirmBeforeApplyingMigration(false)`.

```
@Library(['terraform-pipeline']) _
Jenkinsfile.init(this, Customizations)
// Use the FlywayCommand to modify specific options like `user`, `password`, `locations`, and `url`
FlywayCommand.withUser("\$TF_VAR_USER")
             .withPassword("\$TF_VAR_PASSWORD")
             .withLocations("filesystem:`pwd`/migrations")
             .withUrl("`terraform output jdbc_url`")
FlywayMigrationPlugin.confirmBeforeApplyingMigration(false)
                     .init()

Jenkinsfile.init(this, Customizations)
Jenkinsfile.defaultNodeName = 'docker'
ConditionalApplyPlugin.withApplyOnEnvironment('qa')

AgentNodePlugin.withAgentDockerImage("custom-terraform-fakedb")
               .withAgentDockerfile()
               .withAgentDockerImageOptions("--entrypoint=''")
               .init()

def validate = new TerraformValidateStage()
def deployQa = new TerraformEnvironmentStage('qa')
def deployUat = new TerraformEnvironmentStage('uat')

validate.then(deployQa)
        .then(deployUat)
        .build()
```
