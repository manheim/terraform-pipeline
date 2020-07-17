class DefaultStrategy {

    private String strategyName = "apply"
    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformApplyCommand applyCommand

    DefaultStrategy() {
    }

    public String getStrategyName() {
        return strategyName
    }

    public Closure createPipelineClosure(String environment) {
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        applyCommand = TerraformApplyCommand.instanceFor(environment)

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

                    decorations.apply("Around-${APPLY}") {
                        stage("${APPLY}-${environment}") {
                            decorations.apply(APPLY) {
                                sh initCommand.toString()
                                sh applyCommand.toString()
                            }
                        }
                    }
                }
            }
        }
    }
}