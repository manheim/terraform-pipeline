import static TerraformEnvironmentStage.PLAN_COMMAND

class TerraformTaintPlugin implements TerraformEnvironmentStagePlugin, TerraformTaintCommandPlugin, TerraformUntaintCommandPlugin, Resettable {
    private static DEFAULT_BRANCHES = ['master']
    private static branches = DEFAULT_BRANCHES

    public static void init() {
        TerraformTaintPlugin plugin = new TerraformTaintPlugin()

        BuildWithParametersPlugin.withStringParameter([
            name: "TAINT_RESOURCE",
            description: 'Run `terraform taint` on the resource specified prior to planning and applying.'
        ])

        BuildWithParametersPlugin.withStringParameter([
            name: "UNTAINT_RESOURCE",
            description: 'Run `terraform untaint` on the resource specified prior to planning and applying.'
        ])

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformTaintCommand.addPlugin(plugin)
        TerraformUntaintCommand.addPlugin(plugin)
    }

    public static onBranch(String branchName) {
        this.branches << branchName
        return this
    }

    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN_COMMAND, runTerraformUntaintCommand(stage.getEnvironment()))
        stage.decorate(PLAN_COMMAND, runTerraformTaintCommand(stage.getEnvironment()))
    }

    public void apply(TerraformTaintCommand command) {
        def resource = Jenkinsfile.instance.getEnv().TAINT_RESOURCE
        if (resource) {
            command.withResource(resource)
        }
    }

    public void apply(TerraformUntaintCommand command) {
        def resource = Jenkinsfile.instance.getEnv().UNTAINT_RESOURCE
        if (resource) {
            command.withResource(resource)
        }
    }

    public boolean shouldApply() {
        // Check branches
        if (branches.contains(Jenkinsfile.instance.getEnv().BRANCH_NAME)) {
            return true
        } else if (null == Jenkinsfile.instance.getEnv().BRANCH_NAME) {
            return true
        }

        return false
    }

    public Closure runTerraformTaintCommand(String environment) {
        def taintCommand = TerraformTaintCommand.instanceFor(environment)
        return { closure ->
            if (shouldApply()) {
                echo "Running '${taintCommand.toString()}'. TerraformTaintPlugin is enabled."
                sh taintCommand.toString()
            }
            closure()
        }
    }

    public Closure runTerraformUntaintCommand(String environment) {
        def untaintCommand = TerraformUntaintCommand.instanceFor(environment)
        return { closure ->
            if (shouldApply()) {
                echo "Running '${untaintCommand.toString()}'. TerraformTaintPlugin is enabled."
                sh untaintCommand.toString()
            }
            closure()
        }
    }

    public static reset() {
        this.branches = DEFAULT_BRANCHES.clone()
    }
}
