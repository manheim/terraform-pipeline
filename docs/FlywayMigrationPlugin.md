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

If needed, flyway configuration can be set through the Plugin, or FlywayCommand.

```
@Library(['terraform-pipeline']) _
Jenkinsfile.init(this, Customizations)
// Use the FlywayCommand to modify specific options like `locations` and `url`
FlywayCommand.withLocations("filesystem:`pwd`/migrations")
             .withUrl("`terraform output jdbc_url`")
// Flyway automatically picks up specific enviornment variables (FLYWAY_USER, FLYWAY_PASSWORD)
//     Sets FLYWAY_USER to the value of $TF_VAR_MIGRATION_USER
//     Sets FLYWAY_PASSWORD to the value of $TF_VAR_MIGRATION_PASSWORD
FlywayMigrationPlugin.withUserVariable('TF_VAR_MIGRATION_USER')
                     .withPasswordVariable('TF_VAR_MIGRATION_PASSWORD')
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

