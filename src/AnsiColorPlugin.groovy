import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.APPLY

class AnsiColorPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new AnsiColorPlugin())
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
