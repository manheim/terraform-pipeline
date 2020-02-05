import static TerraformValidateStage.ALL
import static TerraformEnvironmentStage.ALL

public class AgentNodePlugin implements TerraformValidateStagePlugin, TerraformEnvironmentStagePlugin {
    private static String dockerImage
    private static String dockerOptions

    AgentNodePlugin() { }

    public static void init() {
        def plugin = new AgentNodePlugin()

        TerraformValidateStage.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public static AgentNodePlugin withAgentDockerImage(String dockerImage) {
        this.dockerImage = dockerImage
        return this
    }

    public static AgentNodePlugin withAgentDockerImageOptions(String dockerOptions) {
        this.dockerOptions = dockerOptions
        return this
    }

    @Override
    public void apply(TerraformValidateStage stage) {
        stage.decorate(TerraformValidateStage.ALL, addAgent())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(TerraformEnvironmentStage.ALL, addAgent())
    }

    public Closure addAgent() {
        return { closure ->
            if (dockerImage) {
                docker.image(this.dockerImage).inside(this.dockerOptions) {
                    closure()
                }
            } else {
                closure()
            }
        }
    }
}
