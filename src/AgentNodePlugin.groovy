import static TerraformValidateStage.ALL
import static TerraformEnvironmentStage.ALL

public class AgentNodePlugin implements TerraformValidateStagePlugin, TerraformEnvironmentStagePlugin {
    private static String dockerImage
    private static String dockerfile
    private static String dockerBuildOptions
    private static String dockerOptions

    AgentNodePlugin() { }

    public static void init() {
        def plugin = new AgentNodePlugin()

        TerraformValidateStage.addPlugin(plugin)
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public static withAgentDockerImage(String dockerImage) {
        this.dockerImage = dockerImage
        return this
    }

    public static withAgentDockerImageOptions(String dockerOptions) {
        this.dockerOptions = dockerOptions
        return this
    }

    public static withAgentDockerBuildOptions(String dockerBuildOptions) {
        this.dockerBuildOptions = dockerBuildOptions
        return this
    }

    public static withAgentDockerfile(String dockerfile = 'Dockerfile') {
        this.dockerfile = dockerfile
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
            if (this.dockerImage && this.dockerfile == null) {
                docker.image(this.dockerImage).inside(this.dockerOptions) {
                    closure()
                }
            } else if (this.dockerImage && this.dockerfile) {
                docker.build(this.dockerImage, "${this.dockerBuildOptions} -f ${dockerfile} .").inside(this.dockerOptions) {
                    closure()
                }
            } else {
                closure()
            }
        }
    }

    public static void reset() {
        dockerImage = null
        dockerfile = null
        dockerBuildOptions = null
        dockerOptions = null
    }
}
