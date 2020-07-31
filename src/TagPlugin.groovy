class TagPlugin implements TerraformPlanCommandPlugin,
                           TerraformApplyCommandPlugin {
    public static init() {
        def plugin = new TagPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        def tags = getTagsAsString()
        def tagArgument = "-var=\'${tags}\'"

        command.withArgument(tagArgument)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        def tags = getTagsAsString()
        def tagArgument = "-var=\'${tags}\'"

        command.withArgument(tagArgument)
    }

    public String getTagsAsString() {
        return "foo"
    }
}
