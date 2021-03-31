## [FlywayMigrationPlugin](../src/FlywayMigrationPlugin.groovy)

Enable this plugin to run automated database migrations with [Flyway](https://flywaydb.org/).  Flyway can be configured through standard configuration files, or through environment variables.

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

If needed, flyway configuration can be set through the Plugin.

```
@Library(['terraform-pipeline']) _
Jenkinsfile.init(this, Customizations)
// Runs `terraform output jdbc_url_with_database` and sets the output to the environment variable FLYWAY_URL
// Sets FLYWAY_USER to the value of $TF_VAR_MIGRATION_USER
// Sets FLYWAY_PASSWORD to the value of $TF_VAR_MIGRATION_PASSWORD
FlywayMigrationPlugin.convertOutputToEnvironmentVariable('jdbc_url_with_database', 'FLYWAY_URL')
                     .withUser('$TF_VAR_MIGRATION_USER')
                     .withPassword('$TF_VAR_MIGRATION_PASSWORD')
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

