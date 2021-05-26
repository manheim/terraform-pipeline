import static TerraformEnvironmentStage.PLAN_COMMAND

class TerraformImportPlugin implements TerraformEnvironmentStagePlugin, TerraformImportCommandPlugin {

    public static void init() {
        TerraformImportPlugin plugin = new TerraformImportPlugin()

        BuildWithParametersPlugin.withStringParameter([
            name: "IMPORT_RESOURCE",
            description: "Run `terraform import` on the resource specified prior to planning and applying."
        ])
        BuildWithParametersPlugin.withStringParameter([
            name: "IMPORT_TARGET_PATH",
            description: "The path in the Terraform state to import the spcified resource to."
        ])
        BuildWithParametersPlugin.withStringParameter([
            name: "IMPORT_ENVIRONMENT",
            description: "The environment in which to run the import."
        ])

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformImportCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        def resource = Jenkinsfile.instance.getEnv().IMPORT_RESOURCE
        def targetPath = Jenkinsfile.instance.getEnv().IMPORT_TARGET_PATH
        def environment = Jenkinsfile.instance.getEnv().IMPORT_ENVIRONMENT
        if (resource && targetPath && stage.environment == environment) {
            stage.decorate(PLAN_COMMAND, runTerraformImportCommand(stage.environment))
        }
    }

    @Override
    public void apply(TerraformImportCommand command) {
        def resource = Jenkinsfile.instance.getEnv().IMPORT_RESOURCE
        def targetPath = Jenkinsfile.instance.getEnv().IMPORT_TARGET_PATH
        if (resource && targetPath) {
            command.withResource(resource).withTargetPath(targetPath)
        }
    }

    public Closure runTerraformImportCommand(String environment) {
        def importCommand = TerraformImportCommand.instanceFor(environment)
        return { closure ->
            if (importCommand.resource) {
                echo "Running '${importCommand.toString()}'. TerraformImportPlugin is enabled."
                sh importCommand.toString()
            }
            closure()
        }
    }
}
