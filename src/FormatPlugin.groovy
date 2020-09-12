class FormatPlugin implements TerraformValidateStagePlugin {
    public static init() {
        TerraformValidateStage.addPlugin(new FormatPlugin())
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
