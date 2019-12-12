class TfvarsFilesPlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin {

    static String directory = "."

    static withDirectory(String directory) {
        TfvarsFilesPlugin.directory = directory
        return this
    }

    static void init() {
        def plugin = new TfvarsFilesPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    private originalContext = Jenkinsfile.instance.original

    @Override
    void apply(TerraformApplyCommand command) {
        def environmentVarFile = "${directory}/${command.environment}.tfvars"
        if(originalContext.fileExists(environmentVarFile)) {
            command.withArgument("-var-file=${environmentVarFile}")
        } else {
            originalContext.echo "${environmentVarFile} does not exist."
        }
    }

    @Override
    void apply(TerraformPlanCommand command) {
        def environmentVarFile = "${directory}/${command.environment}.tfvars"
        if(originalContext.fileExists(environmentVarFile)) {
            command.withArgument("-var-file=${environmentVarFile}")
        } else {
            originalContext.echo "${environmentVarFile} does not exist."
        }
    }
}
