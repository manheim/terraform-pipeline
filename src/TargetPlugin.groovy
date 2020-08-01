class TargetPlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin {
    public static void init() {
        TargetPlugin plugin = new TargetPlugin()

        Jenkinsfile.instance.addParam([
            $class: 'hudson.model.StringParameterDefinition',
            name: "RESOURCE_TARGETS",
            defaultValue: '',
            description: 'comma-separated list of resource addresses to pass to plan and apply "-target=" parameters'
        ])

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
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
}
