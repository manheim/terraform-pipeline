import static TerraformValidateStage.ALL
import static TerraformEnvironmentStage.ALL

public class AgentNodePlugin implements TerraformValidateStagePlugin, TerraformEnvironmentStagePlugin {
    private static String dockerImage
    private static boolean withDockerfile
    private static String dockerBuildOptions
    private static String dockerOptions

    AgentNodePlugin() { }

    public static void init() {
        def plugin = new AgentNodePlugin()

        TerraformValidateStage.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public static AgentNodePlugin withAgentDockerImage(String dockerImage, withDockerfile=false) {
        this.dockerImage    = dockerImage
        this.withDockerfile = withDockerfile
        return this
    }

    public static AgentNodePlugin withAgentDockerImageOptions(String dockerOptions) {
        this.dockerOptions = dockerOptions
        return this
    }

    public static AgentNodePlugin withAgentDockerBuildOptions(String dockerBuildOptions) {
        this.dockerBuildOptions = dockerBuildOptions
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
            if (this.dockerImage && this.withDockerfile == false) {
                docker.image(this.dockerImage).inside(this.dockerOptions) {
                    closure()
                }
            } else if (this.dockerImage && this.withDockerfile == true) {
                docker.build(this.dockerImage, "${this.dockerBuildOptions} -f Dockerfile .").inside(this.dockerOptions) {
                    closure()
                }
            } else {
                closure()
            }
        }
    }
}
