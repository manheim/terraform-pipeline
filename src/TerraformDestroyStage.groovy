class TerraformDestroyStage extends TerraformEnvironmentStage {
    private Jenkinsfile jenkinsfile
    private String environment
    private StageDecorations decorations
    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformDestroyCommand destroyCommand

    public static final String ALL = 'all'
    public static final String PLAN = 'plan'
    public static final String CONFIRM = 'confirm'
    public static final String DESTROY = 'destroy'

    TerraformDestroyStage(String environment) {
        this.environment = environment
        this.jenkinsfile = Jenkinsfile.instance
        this.decorations = new StageDecorations()
    }

    @Override
    private Closure pipelineConfiguration() {
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        planCommand = planCommand.withArgument("-destroy")
        destroyCommand = TerraformDestroyCommand.instanceFor(environment)

        applyPlugins()

        def String environment = this.environment
        return { ->
            node(jenkinsfile.getNodeName()) {
                deleteDir()
                checkout(scm)

                decorations.apply(ALL) {
                    stage("${PLAN}-${environment}") {
                        decorations.apply(PLAN) {
                            sh initCommand.toString()
                            sh planCommand.toString()
                        }
                    }

                    decorations.apply("Around-${CONFIRM}") {
                        stage("${CONFIRM}-${environment}") {
                            decorations.apply(CONFIRM) {
                                echo "Approved"
                            }
                        }
                    }

                    decorations.apply("Around-${DESTROY}") {
                        stage("${DESTROY}-${environment}") {
                            decorations.apply(DESTROY) {
                                sh initCommand.toString()
                                sh destroyCommand.toString()
                            }
                        }
                    }
                }
            }
        }
    }

}
