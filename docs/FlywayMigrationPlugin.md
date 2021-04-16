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

