class TerraformPluginVersion15 extends TerraformPluginVersion12 {

    public void apply(TerraformValidateCommand command) {
        super.apply(command.withChangeDirectoryFlag())
    }

    public void apply(TerraformInitCommand command) {
        super.apply(command.withChangeDirectoryFlag())
    }

    public void apply(TerraformPlanCommand command) {
        super.apply(command.withChangeDirectoryFlag())
    }

    public void apply(TerraformApplyCommand command) {
        super.apply(command.withChangeDirectoryFlag())
    }
}
