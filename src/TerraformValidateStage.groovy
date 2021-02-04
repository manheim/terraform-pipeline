class TerraformValidateStage implements Stage, DecoratableStage {
    private Jenkinsfile jenkinsfile
    private StageDecorations decorations

    private static globalPlugins = []

    public static final String ALL = 'all'
    public static final String VALIDATE = 'validate'

    public TerraformValidateStage() {
        this.jenkinsfile = Jenkinsfile.instance
        this.decorations = new StageDecorations()
    }

    public Stage then(Stage nextStage) {
        return new BuildGraph(this).then(nextStage)
    }

    public void build() {
        Jenkinsfile.build(pipelineConfiguration())
    }

    public Closure pipelineConfiguration() {
        applyPlugins()

        def validateCommand = TerraformValidateCommand.instance()

        return {
            node(jenkinsfile.getNodeName()) {
                deleteDir()
                checkout(scm)

                decorations.apply(ALL) {
                    stage("validate") {
                        decorations.apply(VALIDATE) {
                            sh validateCommand.toString()
                        }
                    }
                }
            }
        }
    }

    public void decorate(Closure decoration) {
        decorations.add(ALL, decoration)
    }

    public decorate(String stageName, Closure decoration) {
        decorations.add(stageName, decoration)
    }

    public static addPlugin(plugin) {
        this.globalPlugins << plugin
    }

    public void applyPlugins() {
        for (plugin in globalPlugins) {
            plugin.apply(this)
        }
    }

    public static getPlugins() {
        return this.globalPlugins
    }

    public static void reset() {
        this.globalPlugins = []
    }
}
