class TagPlugin implements TerraformPlanCommandPlugin,
                           TerraformApplyCommandPlugin {

    private static variableName
    private List tagClosures = []

    public static init() {
        def plugin = new TagPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        def tagString = getTagsAsString()
        def variableName = getVariableName()
        def tagArgument = "-var=\'${variableName}=${tagString}\'"

        command.withArgument(tagArgument)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        def tagString = getTagsAsString()
        def variableName = getVariableName()
        def tagArgument = "-var=\'${variableName}=${tagString}\'"

        command.withArgument(tagArgument)
    }

    public static withVariableName(String variableName) {
        this.variableName = variableName
    }

    private static getVariableName() {
        return variableName ?: 'tags'
    }

    public withTag(String key, String value) {
        tagClosures << { -> "\"${key}\":\"${value}\"" }
    }

    public String getTagsAsString() {
        def result = tagClosures.collect { it.call() }.join(',')
        return "{${result}}"
    }

    public static reset() {
        variableName = null
    }
}
