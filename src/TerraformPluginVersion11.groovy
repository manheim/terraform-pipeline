class TerraformPluginVersion11 implements TerraformValidateCommandPlugin {

    @Override
    public void apply(TerraformValidateCommand command) {
        command.withArgument('-check-variables=false')
    }
}
