class TerraformPluginVersion12 implements TerraformValidateStagePlugin {

    @Override
    public void apply(TerraformValidateStage validateStage) {
        validateStage.decorate(TerraformValidateStage.VALIDATE, addInitBefore())
    }

    public Closure addInitBefore() {
        return { closure ->
            def initCommand = initCommandForValidate()
            sh initCommand.toString()

            closure()
        }
    }

    public static TerraformInitCommand initCommandForValidate() {
        return TerraformInitCommand.instanceFor('validate').withoutBackend()
    }
}
