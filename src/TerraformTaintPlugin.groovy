import static TerraformEnvironmentStage.PLAN_COMMAND

class TerraformTaintPlugin implements TerraformEnvironmentStagePlugin, TerraformTaintCommandPlugin, TerraformUntaintCommandPlugin, Resettable {
    public static String origin_repo = ''
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

    public static onlyOnOriginRepo(String origin_repo) {
        this.origin_repo = origin_repo
        return this
    }

    public static onMasterOnly() {
        this.branches = ['master']
        return this
    }

    public static onBranch(String branchName) {
        this.branches << branchName
        return this
    }

    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN_COMMAND, runTerraformTaintCommand(stage.getEnvironment()))
        stage.decorate(PLAN_COMMAND, runTerraformUntaintCommand(stage.getEnvironment()))
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
        def apply = false
        def current_repo = repoSlug(Jenkinsfile.instance.getEnv().GIT_URL)

        // Check branches
        if (branches.contains(Jenkinsfile.instance.getEnv().BRANCH_NAME)) {
            apply = true
        } else if (null == Jenkinsfile.instance.getEnv().BRANCH_NAME) {
            apply = true
        }

        // Check repo slug
        if (current_repo.toLowerCase() == this.origin_repo.toLowerCase()) {
            apply = apply && true
        } else if (! this.origin_repo) { // We assume that no origin repo means run always
            apply = apply && true
        } else { // We don't have a match, so reset apply
            apply = false
        }

        return apply
    }

    private String repoSlug(String originUrl) {
        if (! originUrl.endsWith('.git')) { originUrl = "${originUrl}.git" }
        def giturl = originUrl =~ /^.*@.*[:\/]([^\/]+\/.*)\.git$/
        def httpurl = originUrl =~ /^https?:\/\/[^\/]+\/([^\/]+\/[^\/]+)\.git$/
        if ( giturl.matches() ) {
            return giturl[0][1]
        } else if ( httpurl.matches() ) {
            return httpurl[0][1]
        }
        return ""
    }

    public Closure runTerraformTaintCommand(String environment) {
        def taintCommand = TerraformTaintCommand.instanceFor(environment)
        return { closure ->
            if (taintCommand.resource && shouldApply()) {
                echo "Running '${taintCommand.toString()}'. TerraformTaintPlugin is enabled."
                sh taintCommand.toString()
            }
            closure()
        }
    }

    public Closure runTerraformUntaintCommand(String environment) {
        def untaintCommand = TerraformUntaintCommand.instanceFor(environment)
        return { closure ->
            if (untaintCommand.resource && shouldApply()) {
                echo "Running '${untaintCommand.toString()}'. TerraformTaintPlugin is enabled."
                sh untaintCommand.toString()
            }
            closure()
        }
    }

    public static reset() {
        this.origin_repo = ''
        this.branches = DEFAULT_BRANCHES.clone()
    }
}
