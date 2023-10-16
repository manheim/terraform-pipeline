import static TerraformEnvironmentStage.ALL

class BuildStage implements Stage, DecoratableStage, TerraformEnvironmentStagePlugin, Resettable {
    private final String ARTIFACT_STASH_KEY = 'buildArtifact'

    public String buildCommand

    private String artifactIncludePattern
    private StageDecorations decorations
    private Jenkinsfile jenkinsfile

    private static plugins = []

    public BuildStage() {
        this("./build.sh")
    }

    public BuildStage(String buildCommand) {
        this.buildCommand = buildCommand
        this.jenkinsfile = Jenkinsfile.instance
        this.decorations = new StageDecorations()
    }

    public BuildStage saveArtifact(String artifactIncludePattern) {
        this.artifactIncludePattern = artifactIncludePattern
        TerraformEnvironmentStage.addPlugin(this)
        RegressionStage.addPlugin(this)
        return this
    }

    public Stage then(Stage nextStage) {
        return new BuildGraph(this).then(nextStage)
    }

    public void build() {
        Jenkinsfile.build(pipelineConfiguration())
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(ALL, unstashArtifact(ARTIFACT_STASH_KEY))
    }

    @Override
    public void apply(RegressionStage stage) {
        stage.decorate(ALL, unstashArtifact(ARTIFACT_STASH_KEY))
    }

    private Closure unstashArtifact(String artifactStashKey) {
        return { closure ->
            unstash "${artifactStashKey}"
            closure()
        }
    }

    public void decorate(Closure decoration) {
        decorations.add(decoration)
    }

    protected Closure pipelineConfiguration() {
        applyPlugins()

        return {
            node(jenkinsfile.getNodeName()) {
                stage("build") {
                    decorations.apply {
                        checkout(scm)
                        sh buildCommand
                        if (artifactIncludePattern != null) {
                            stash includes: artifactIncludePattern, name: ARTIFACT_STASH_KEY
                        }
                    }
                }
            }
        }
    }

    public static getPlugins() {
        return plugins
    }

    public static void reset() {
        this.plugins = []
    }

    public static void addPlugin(plugin) {
        plugins << plugin
    }

    public void applyPlugins() {
        for (plugin in plugins) {
            plugin.apply(this)
        }
    }
}
