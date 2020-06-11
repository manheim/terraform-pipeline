class TargetPlugin implements TerraformPlanCommandPlugin {
    public static void init() {
        TargetPlugin plugin = new TargetPlugin()

        TerraformPlanCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withSuffix("| landscape")
    }

}
