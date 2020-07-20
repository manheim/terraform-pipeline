import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.CONFIRM
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.DESTROY

class DeployAndDestroyStrategy {

    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformApplyCommand applyCommand

    private TerraformPlanCommand planDestroyCommand
    private TerraformApplyCommand destroyCommand

    private Jenkinsfile jenkinsfile

    public Closure createPipelineClosure(String environment, StageDecorations decorations) {
        // First stage: apply
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        applyCommand = TerraformApplyCommand.instanceFor(environment)

        // Last stage: destroy
        planDestroyCommand = TerraformPlanCommand.instanceFor(environment)
        planDestroyCommand = planDestroyCommand.withArgument("-destroy")
        destroyCommand = TerraformApplyCommand.instanceFor(environment)
        destroyCommand = destroyCommand.withCommand("destroy")

        jenkinsfile = Jenkinsfile.instance

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

                    stage("${PLAN}-${DESTROY}-${environment}") {
                        decorations.apply(PLAN) {
                            sh initCommand.toString()
                            sh planDestroyCommand.toString()
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
