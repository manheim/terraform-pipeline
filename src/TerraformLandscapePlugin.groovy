class TerraformLandscapePlugin implements TerraformPlanCommandPlugin {
    public static void init() {
        TerraformLandscapePlugin plugin = new TerraformLandscapePlugin()

        TerraformPlanCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withPrefix("gem list installed | grep terraform_landscape &&")
        command.withSuffix(" | landscape")
    }

}
