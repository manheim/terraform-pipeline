import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.CONFIRM
import static TerraformEnvironmentStage.APPLY

class DestroyStrategy {

    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformApplyCommand destroyCommand
    private Jenkinsfile jenkinsfile

    public Closure createPipelineClosure(String environment, StageDecorations decorations) {
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        planCommand = planCommand.withArgument("-destroy")
        destroyCommand = TerraformApplyCommand.instanceFor(environment)
        destroyCommand = destroyCommand.withCommand("destroy")
        jenkinsfile = Jenkinsfile.instance

        return { ->
            node(jenkinsfile.getNodeName()) {
                deleteDir()
                checkout(scm)

                decorations.apply(ALL) {
                    stage("${PLAN}-destroy-${environment}") {
                        decorations.apply(PLAN) {
                            sh initCommand.toString()
                            sh planCommand.toString()
                        }
                    }

                    decorations.apply("Around-${CONFIRM}") {
                        stage("${CONFIRM}-destroy-${environment}") {
                            decorations.apply(CONFIRM) {
                                echo "Approved"
                            }
                        }
                    }

                    decorations.apply("Around-destroy") {
                        stage("destroy-${environment}") {
                            decorations.apply(APPLY) {
                                sh destroyCommand.toString()
                            }
                        }
                    }
                }
            }
        }
    }
}
