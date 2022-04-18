class TerraformApplyCommand implements TerraformCommand, Resettable {
    private boolean input = false
    private String terraformBinary = "terraform"
    private String command = "apply"
    String environment
    private prefixes = []
    private suffixes = []
    private args = []
    private String directory
    private String planFile
    private boolean chdir_flag = false
    private Closure variablePattern
    private Closure mapPattern
    private static plugins = []
    private appliedPlugins = []

    public TerraformApplyCommand(String environment) {
        this.environment = environment
    }

    public TerraformApplyCommand withCommand(String newCommand) {
        this.command = newCommand
        return this
    }

    public TerraformApplyCommand withInput(boolean input) {
        this.input = input
        return this
    }

    public TerraformApplyCommand withArgument(String arg) {
        this.args << arg
        return this
    }

    public TerraformApplyCommand withVariable(String key, Map value) {
        return withVariable(key, convertMapToCliString(value))
    }

    public TerraformApplyCommand withVariable(String key, String value) {
        def pattern = variablePattern ?: { myKey, myValue -> "-var '${myKey}=${myValue}'" }
        this.args << pattern.call(key, value).toString()
        return this
    }

    public TerraformApplyCommand withVariableFile(String key, Map values) {
        String fileName = "${this.environment}-${key}.tfvars"
        String value = convertMapToCliString(values)
        Jenkinsfile.writeFile(fileName, "${key}=${value}")
        return withVariableFile(fileName)
    }

    public TerraformApplyCommand withVariableFile(String fileName) {
        this.args << "-var-file=./${fileName}"
        return this
    }

    public TerraformApplyCommand withVariablePattern(Closure pattern) {
        this.variablePattern = pattern
        return this
    }

    public String convertMapToCliString(Map newMap) {
        def pattern = mapPattern ?: { map ->
            def result = map.collect { key, value -> "${key}=\"${value}\"" }.join(',')
            return "{${result}}"
        }

        return pattern.call(newMap)
    }

    public TerraformApplyCommand withMapPattern(Closure pattern) {
        this.mapPattern = pattern
        return this
    }

    public TerraformApplyCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformApplyCommand withSuffix(String suffix) {
        suffixes << suffix
        return this
    }

    public TerraformApplyCommand withDirectory(String directory) {
        this.directory = directory
        return this
    }

    public TerraformApplyCommand withPlanFile(String planFile) {
        this.planFile = planFile
        return this
    }

    public TerraformApplyCommand withChangeDirectoryFlag() {
        this.chdir_flag = true
        return this
    }

    public String toString() {
        applyPlugins()
        def pieces = []
        pieces += prefixes
        pieces << terraformBinary
        if (directory && chdir_flag) {
            pieces << "-chdir=${directory}"
        }
        pieces << command
        if (!input) {
            pieces << "-input=false"
        }
        pieces += args
        if (directory && !chdir_flag && !planFile) {
            pieces << directory
        }

        if (planFile) {
            pieces << planFile
        }

        pieces += suffixes

        return pieces.join(' ')
    }

    public static TerraformApplyCommand instanceFor(String environment) {
        return new TerraformApplyCommand(environment)
            .withInput(false)
            .withArgument("-auto-approve")
    }

    public String getEnvironment() {
        return environment
    }

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformApplyCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformApplyCommandPlugin plugin) {
        plugins << plugin
    }

    public static void setPlugins(plugins) {
        this.plugins = plugins
    }

    public static getPlugins() {
        return plugins
    }

    public static void reset() {
        this.plugins = []
    }
}
