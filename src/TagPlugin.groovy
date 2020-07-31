class TagPlugin implements TerraformPlanCommandPlugin,
                           TerraformApplyCommandPlugin {

    private static variableName
    private static List tagClosures = []

    public static init() {
        def plugin = new TagPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        applyToCommand(command)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        applyToCommand(command)
    }

    private void applyToCommand(command) {
        def tagString = getTagsAsString(command)
        def variableName = getVariableName()
        def tagArgument = "-var=\'${variableName}=${tagString}\'"

        command.withArgument(tagArgument)
    }

    public static withVariableName(String variableName) {
        this.variableName = variableName
    }

    public static withEnvironmentTag(String tagKey = 'environment') {
        tagClosures << { command -> "\"${tagKey}\":\"${command.getEnvironment()}\"" }
        return this
    }

    public static withTag(String key, String value) {
        tagClosures << { command -> "\"${key}\":\"${value}\"" }
        return this
    }

    public static withTagFromFile(String key, String filename) {
        tagClosures << { command -> "\"${key}\":\"${Jenkinsfile.readFile(filename)}\"" }
        return this
    }

    public static withTagFromEnvironmentVariable(String key, String variable) {
        tagClosures << { command -> "\"${key}\":\"${Jenkinsfile.getEnvironmentVariable(variable)}\"" }
        return this
    }

    private static getVariableName() {
        return variableName ?: 'tags'
    }

    public String getTagsAsString(TerraformCommand command) {
        def result = tagClosures.collect { it.call(command) }.join(',')
        return "{${result}}"
    }

    public static reset() {
        tagClosures = []
        variableName = null
    }
}
