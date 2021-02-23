interface TerraformTaintCommandPlugin {
    public void apply(TerraformTaintCommand command)
}

public class TerraformTaintCommandPluginImpl implements TerraformEnvironmentStagePlugin, TerraformTaintCommandPlugin {
    private String[] resources

    public static void init() {
        TerraformTaintCommandPlugin plugin = new TerraformTaintCommandPluginImpl();

        BuildWithParametersPlugin.withStringParameter([
            name: "TAINT_RESOURCES",
            description: 'Run \`terraform taint\` on the resources specified prior to planning and applying.'
        ])

        BuildWithParametersPlugin.withStringParameter([
            name: "UNTAINT_RESOURCES",
            description: 'Run \`terraform untaint\` on the resources specified prior to planning and applying.'
        ])

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public Closure onlyOnExpectedBranch() {}
    public boolean shouldApply() {}
}