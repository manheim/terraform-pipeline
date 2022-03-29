class TagPlugin implements TerraformPlanCommandPlugin,
                           TerraformApplyCommandPlugin,
                           Resettable {

    private static variableName
    private static disableOnApply = false
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
        if (disableOnApply) {
            return
        }

        applyToCommand(command)
    }

    private void applyToCommand(command) {
        String variableName = getVariableName()
        Map tags = getTags(command)

        command.withVariableFile(variableName, tags)
    }

    public static withVariableName(String variableName) {
        this.variableName = variableName
    }

    public static withEnvironmentTag(String tagKey = 'environment') {
        tagClosures << { command ->
            Map tag = [:]
            tag[tagKey] = command.getEnvironment()
            tag
        }
        return this
    }

    public static withTag(String key, String value) {
        tagClosures << { command ->
            Map tag = [:]
            tag[key] = value
            tag
        }
        return this
    }

    public static withTagFromFile(String key, String filename) {
        tagClosures << { command ->
            Map tag = [:]
            tag[key] = Jenkinsfile.readFile(filename)
            tag
        }
        return this
    }

    public static withTagFromEnvironmentVariable(String key, String variable) {
        tagClosures << { command ->
            Map tag = [:]
            tag[key] = Jenkinsfile.getEnvironmentVariable(variable)
            tag
        }
        return this
    }

    public static disableOnApply() {
        disableOnApply = true
        return this
    }

    private static getVariableName() {
        return variableName ?: 'tags'
    }

    public Map getTags(TerraformCommand command) {
        return tagClosures.inject([:]) { memo, tagClosure ->
            memo + tagClosure.call(command)
        }
    }

    public static reset() {
        tagClosures = []
        variableName = null
        disableOnApply = false
    }
}
