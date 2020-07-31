class TagPlugin implements TerraformPlanCommandPlugin,
                           TerraformApplyCommandPlugin {

    private Map tags = [:]

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

    public withTag(String key, String value) {
        tags.put(key, value)
    }

    public String getTagsAsString() {
        def result = tags.collect { "\"${it.key}\":\"${it.value}\"" }.join(',')
        return "{${result}}"
    }
}
