class TfvarsFilesPlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin, Resettable {

    static String directory = "."
    static List<String> globalFiles = []

    static withDirectory(String directory) {
        TfvarsFilesPlugin.directory = directory
        return this
    }

    static withGlobalVarFile(String fileName) {
        TfvarsFilesPlugin.globalFiles << fileName
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
        applyToCommand(command)
    }

    @Override
    void apply(TerraformPlanCommand command) {
        applyToCommand(command)
    }

    void applyToCommand(command) {
        def files = globalFiles.collect { file ->
            return "${directory}/${file}"
        } + "${directory}/${command.environment}.tfvars"

        files.each { file ->
            if (originalContext.fileExists(file)) {
                command.withArgument("-var-file=${file}")
            } else {
                originalContext.echo "${file} does not exist."
            }
        }
    }

    public static void reset() {
        directory = "."
        globalFiles = []
    }
}
