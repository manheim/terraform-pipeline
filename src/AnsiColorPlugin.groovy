import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.APPLY

class AnsiColorPlugin implements TerraformValidateStagePlugin, TerraformEnvironmentStagePlugin {

    public static void init() {
        TerraformValidateStage.addPlugin(new AnsiColorPlugin())
        TerraformEnvironmentStage.addPlugin(new AnsiColorPlugin())
    }

    @Override
    public void apply(TerraformValidateStage stage) {
        stage.decorate(addColor())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN, addColor())
        stage.decorate(APPLY, addColor())
    }

    public Closure addColor() {
        return { closure -> ansiColor('xterm') { closure() } }
    }

}
