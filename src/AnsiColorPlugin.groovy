import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.DESTROY

class AnsiColorPlugin implements TerraformEnvironmentStagePlugin {

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new AnsiColorPlugin())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN, addColor())
        stage.decorate(APPLY, addColor())
        stage.decorate(DESTROY, addColor())
    }

    public static Closure addColor() {
        return { closure -> ansiColor('xterm') { closure() } }
    }

}
