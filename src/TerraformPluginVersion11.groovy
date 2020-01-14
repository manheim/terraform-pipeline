class TerraformPluginVersion11 extends TerraformPluginVersion {

    @Override
    public void apply(TerraformValidateCommand command) {
        command.withArgument('-check-variables=false')
    }
}
