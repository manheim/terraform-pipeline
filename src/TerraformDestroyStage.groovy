class TerraformDestroyStage extends TerraformEnvironmentStage {
    private TerraformDestroyCommand destroyCommand
    public static final String DESTROY = 'destroy'

    TerraformDestroyStage(String environment) {
        super(environment)
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
                    stage("${PLAN}-${DESTROY}-${environment}") {
                        decorations.apply(PLAN) {
                            sh initCommand.toString()
                            sh planCommand.toString()
                        }
                    }

                    decorations.apply("Around-${CONFIRM}") {
                        stage("${CONFIRM}-${DESTROY}-${environment}") {
                            decorations.apply(CONFIRM) {
                                echo "Approved"
                            }
                        }
                    }

                    decorations.apply("Around-${DESTROY}") {
                        stage("${DESTROY}-${environment}") {
                            decorations.apply(DESTROY) {
                                sh destroyCommand.toString()
                            }
                        }
                    }
                }
            }
        }
    }

}
