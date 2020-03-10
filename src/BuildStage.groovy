import static TerraformEnvironmentStage.ALL

class BuildStage implements Stage, TerraformEnvironmentStagePlugin {
    private final String ARTIFACT_STASH_KEY = 'buildArtifact'

    public String buildCommand

    private String artifactIncludePattern
    private Closure existingDecorations
    private Jenkinsfile jenkinsfile

    private static plugins = []

    public BuildStage() {
        this("./build.sh")
    }

    public BuildStage(String buildCommand) {
        this.buildCommand = buildCommand
        this.jenkinsfile = Jenkinsfile.instance
    }

    public BuildStage saveArtifact(String artifactIncludePattern) {
        this.artifactIncludePattern = artifactIncludePattern
        TerraformEnvironmentStage.addPlugin(this)
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

    private Closure unstashArtifact(String artifactStashKey) {
        return { closure ->
            unstash "${artifactStashKey}"
            closure()
        }
    }

    protected Closure pipelineConfiguration() {
        applyPlugins()

        return {
            node(jenkinsfile.getNodeName()) {
                stage("build") {
                    applyDecorations(delegate) {
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

    private void applyDecorations(delegate, Closure stageClosure) {
        if (existingDecorations != null) {
            existingDecorations.delegate = delegate
            existingDecorations(stageClosure)
        } else {
            stageClosure.delegate = delegate
            stageClosure()
        }
    }

    public static getPlugins() {
        return plugins
    }

    public static void resetPlugins() {
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

    public void decorate(Closure decoration) {
        if (existingDecorations == null) {
            existingDecorations = decoration
            existingDecorations.resolveStrategy = Closure.DELEGATE_FIRST
        } else {
            def newDecoration = { stage ->
                decoration.delegate = delegate
                decoration.resolveStrategy = Closure.DELEGATE_FIRST
                decoration() {
                    stage.delegate = delegate
                    existingDecorations.delegate = delegate
                    existingDecorations(stage)
                }
            }
            existingDecorations = newDecoration
        }
    }
}
