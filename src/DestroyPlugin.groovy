import static TerraformEnvironmentStage.DESTROY
import ConditionalApplyPlugin.onlyOnExpectedBranch

class DestroyPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        DestroyPlugin plugin = new DestroyPlugin()

        ConfirmApplyPlugin.withConfirmMessage('WARNING! Are you absolutely sure the plan above is correct? Your environment will be IMMEDIATELY DESTROYED via "terraform destroy"')
        ConfirmApplyPlugin.withOkMessage("Run terraform DESTROY now")

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.withStrategy(new DestroyStrategy())
        stage.decorateAround(DESTROY, onlyOnExpectedBranch())
    }

}
