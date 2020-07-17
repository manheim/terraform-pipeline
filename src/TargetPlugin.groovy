import static TerraformEnvironmentStage.ALL

class TargetPlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin, TerraformEnvironmentStagePlugin {
    public static void init() {
        TargetPlugin plugin = new TargetPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        def targets = Jenkinsfile.instance.getEnv().RESOURCE_TARGETS ?: ''
        targets.split(',')
               .collect { item -> item.trim() }
               .findAll { item -> item != '' }
               .each { item -> command.withArgument("-target ${item}") }
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        def targets = Jenkinsfile.instance.getEnv().RESOURCE_TARGETS ?: ''
        targets.split(',')
               .collect { item -> item.trim() }
               .findAll { item -> item != '' }
               .each { item -> command.withArgument("-target ${item}") }
    }


    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(ALL, addBuildParams())
    }

    public static Closure addBuildParams() {
        return { closure ->
            def params = [
                string(name: 'RESOURCE_TARGETS', defaultValue: '', description: 'comma-separated list of resource addresses to pass to plan and apply "-target=" parameters'),
            ]
            def props = [
                parameters(params)
            ]
            properties(props)

            closure()
        }
    }

}
