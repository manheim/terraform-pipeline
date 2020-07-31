class TagPlugin implements TerraformPlanCommandPlugin,
                           TerraformApplyCommandPlugin {
    public static init() {
        def plugin = new TagPlugin()

        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        println "do the thing"
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        println "do the thing"
    }
}
