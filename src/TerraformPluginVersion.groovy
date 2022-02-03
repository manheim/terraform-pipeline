abstract class TerraformPluginVersion implements TerraformValidateStagePlugin,
                                                 TerraformValidateCommandPlugin,
                                                 TerraformInitCommandPlugin,
                                                 TerraformPlanCommandPlugin,
                                                 TerraformApplyCommandPlugin,
                                                 TerraformFormatCommandPlugin {
    @Override
    public void apply(TerraformValidateStage validateStage) {
    }

    @Override
    public void apply(TerraformValidateCommand command) {
    }

    @Override
    public void apply(TerraformInitCommand command) {
    }

    @Override
    public void apply(TerraformPlanCommand command) {
    }

    @Override
    public void apply(TerraformApplyCommand command) {
    }

    @Override
    public void apply(TerraformFormatCommand command) {
    }
}
