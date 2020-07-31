class TagPlugin implements TerraformPlanCommandPlugin,
                           TerraformApplyCommandPlugin {

    private static variableName
    private Map tags = [:]

    public static init() {
        def plugin = new TagPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        def tags = getTagsAsString()
        def variableName = getVariableName()
        def tagArgument = "-var=\'${variableName}=${tags}\'"

        command.withArgument(tagArgument)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        def tags = getTagsAsString()
        def variableName = getVariableName()
        def tagArgument = "-var=\'${variableName}=${tags}\'"

        command.withArgument(tagArgument)
    }

    public static withVariableName(String variableName) {
        this.variableName = variableName
    }

    private static getVariableName() {
        return variableName ?: 'tags'
    }

    public withTag(String key, String value) {
        tags.put(key, value)
    }

    public String getTagsAsString() {
        def result = tags.collect { "\"${it.key}\":\"${it.value}\"" }.join(',')
        return "{${result}}"
    }

    public static reset() {
        variableName = null
    }
}
