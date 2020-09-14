class ValidateFormatPlugin implements TerraformValidateStagePlugin {
    public static init() {
        TerraformValidateStage.addPlugin(new ValidateFormatPlugin())
        TerraformFormatCommand.withCheck()
    }

    public void apply(TerraformValidateStage stage)  {
        stage.decorate(TerraformValidateStage.VALIDATE, formatClosure())
    }

    public Closure formatClosure() {
        return { closure ->
            closure()
            def formatCommand = new TerraformFormatCommand()
            sh formatCommand.toString()
        }
    }
}
