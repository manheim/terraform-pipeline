class DestroyStrategy {

    private String strategyName = "destroy"
    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformDestroyCommand destroyCommand

    DestroyStrategy() {   
    }

    public String getStrategyName() {
        return strategyName
    }
 
    public Closure createPipelineClosure(String environment) {

        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        planCommand = planCommand.withArgument("-destroy")
        destroyCommand = TerraformDestroyCommand.instanceFor(environment)

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
