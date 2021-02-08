class TerraformDirectoryPlugin implements TerraformInitCommandPlugin, TerraformValidateCommandPlugin, TerraformPlanCommandPlugin, TerraformApplyCommandPlugin, Resettable {

    private static String directory = "./terraform/"

    public static void init() {
        TerraformDirectoryPlugin plugin = new TerraformDirectoryPlugin()

        TerraformInitCommand.addPlugin(plugin)
        TerraformValidateCommand.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    public static withDirectory(String directory) {
        TerraformDirectoryPlugin.directory = directory
        return this
    }

    @Override
    public void apply(TerraformInitCommand command) {
        command.withDirectory(directory)
    }

    @Override
    public void apply(TerraformValidateCommand command) {
        command.withDirectory(directory)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withDirectory(directory)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        command.withDirectory(directory)
    }

    public static reset() {
        directory = "./terraform/"
    }
}
