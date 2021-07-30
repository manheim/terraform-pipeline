import static TerraformValidateStage.ALL
import static TerraformEnvironmentStage.ALL

public class TerraformStartDirectoryPlugin implements TerraformValidateStagePlugin, TerraformEnvironmentStagePlugin, Resettable {
    private static String directory = "./terraform/"

    public static void init() {
        def plugin = new TerraformStartDirectoryPlugin()

        TerraformValidateStage.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public static withDirectory(String directory) {
        this.directory = directory
        return this
    }

    @Override
    public void apply(TerraformValidateStage stage) {
        stage.decorate(TerraformValidateStage.ALL, addDirectory())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(TerraformEnvironmentStage.ALL, addDirectory())
    }

    public Closure addDirectory() {
        return { closure ->
            dir(this.directory) {
                closure()
            }
        }
    }

    public static reset() {
        directory = "./terraform/"
    }
}
