abstract class TerraformPluginVersion implements TerraformValidateStagePlugin,
                                                 TerraformValidateCommandPlugin,
                                                 TerraformPlanCommandPlugin,
                                                 TerraformApplyCommandPlugin {
    @Override
    public void apply(TerraformValidateStage validateStage) {
    }

    @Override
    public void apply(TerraformValidateCommand command) {
    }

    @Override
    public void apply(TerraformPlanCommand command) {
    }

    @Override
    public void apply(TerraformApplyCommand command) {
    }
}
