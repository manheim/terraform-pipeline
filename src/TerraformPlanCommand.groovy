class TerraformPlanCommand implements TerraformCommand, Resettable {
    private boolean input = false
    private String terraformBinary = "terraform"
    private String command = "plan"
    String environment
    private prefixes = []
    private suffixes = []
    private arguments = []
    private String directory
    private boolean chdir_flag = false
    private String errorFile
    private Closure variablePattern
    private Closure mapPattern
    private static plugins = []
    private appliedPlugins = []

    public TerraformPlanCommand(String environment) {
        this.environment = environment
    }

    public TerraformPlanCommand withInput(boolean input) {
        this.input = input
        return this
    }

    public TerraformPlanCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformPlanCommand withSuffix(String suffix) {
        suffixes << suffix
        return this
    }

    public TerraformPlanCommand withDirectory(String directory) {
        this.directory = directory
        return this
    }

    public TerraformPlanCommand withChangeDirectoryFlag() {
        this.chdir_flag = true
        return this
    }

    public TerraformPlanCommand withArgument(String argument) {
        this.arguments << argument
        return this
    }

    public TerraformPlanCommand withVariable(String key, Map value) {
        return withVariable(key, convertMapToCliString(value))
    }

    public TerraformPlanCommand withVariableFile(String key, Map value) {
        String workspace = Jenkinsfile.instance.getEnv()['WORKSPACE']
        def varFile = new File("${workspace}/hello.tfvars")
        if (varFile.createNewFile()) {
            println "Successfully created file"
        } else {
            println "Failed to create file"
        }
        // varFile.append("key")
        return withVariableFile(workspace)
    }

    public TerraformPlanCommand withVariableFile(String fileName) {
        this.arguments << "-var-file=./${fileName}"
        return this
    }

    public TerraformPlanCommand withVariable(String key, String value) {
        def pattern = variablePattern ?: { myKey, myValue -> "-var '${myKey}=${myValue}'" }
        this.arguments << pattern.call(key, value).toString()
        return this
    }

    public TerraformPlanCommand withVariablePattern(Closure pattern) {
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

    public TerraformPlanCommand withMapPattern(Closure pattern) {
        this.mapPattern = pattern
        return this
    }

    public TerraformPlanCommand withStandardErrorRedirection(String errorFile) {
        this.errorFile = errorFile
        return this
    }

    public String toString() {
        applyPlugins()
        def pieces = []
        pieces = pieces + prefixes
        pieces << terraformBinary
        if (directory && chdir_flag) {
            pieces << "-chdir=${directory}"
        }
        pieces << command
        if (!input) {
            pieces << "-input=false"
        }
        pieces += arguments
        if (directory && !chdir_flag) {
            pieces << directory
        }

        // This should be built out to handle more complex redirection
        // and should be standardized across all TerraformCommands
        if (errorFile) {
            pieces << "2>${errorFile}"
        }

        pieces += suffixes

        return pieces.join(' ')
    }

    public static TerraformPlanCommand instanceFor(String environment) {
        return new TerraformPlanCommand(environment)
            .withInput(false)
    }

    public String getEnvironment() {
        return environment
    }

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (TerraformPlanCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(TerraformPlanCommandPlugin plugin) {
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
