import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.CONFIRM
import static TerraformEnvironmentStage.APPLY

class DefaultStrategy {

    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformApplyCommand applyCommand
    private Jenkinsfile jenkinsfile

    public Closure createPipelineClosure(String environment, StageDecorations decorations, params) {
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        applyCommand = TerraformApplyCommand.instanceFor(environment)

        jenkinsfile = Jenkinsfile.instance

        return { ->
            node(jenkinsfile.getNodeName()) {
                deleteDir()
                checkout(scm)
                properties([parameters(params)])

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
