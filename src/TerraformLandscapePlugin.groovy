class TerraformLandscapePlugin implements TerraformPlanCommandPlugin {
    public static void init() {
        TerraformLandscapePlugin plugin = new TerraformLandscapePlugin()

        TerraformPlanCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withPrefix("gem install --no-document --version 0.2.2 terraform_landscape")
        command.withSuffix(" | landscape")
    }

}
