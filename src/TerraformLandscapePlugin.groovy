class TerraformLandscapePlugin implements TerraformPlanCommandPlugin {
    public static void init() {
        TerraformLandscapePlugin plugin = new TerraformLandscapePlugin()

        TerraformPlanCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        println(command)
        command.withSuffix(" | landscape")
    }

}
