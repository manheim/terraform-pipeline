import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.CONFIRM
import static TerraformEnvironmentStage.DESTROY

class DestroyStrategy {

    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformApplyCommand destroyCommand
    private Jenkinsfile jenkinsfile
    private List extraArguments

    DestroyStrategy(List args) {
        this.extraArguments = args
    }

    public Closure createPipelineClosure(String environment, StageDecorations decorations, List params) {
        initCommand = TerraformInitCommand.instanceFor(environment)

        planCommand = TerraformPlanCommand.instanceFor(environment)
        planCommand = planCommand.withArgument("-destroy")

        destroyCommand = TerraformApplyCommand.instanceFor(environment)
        destroyCommand = destroyCommand.withCommand("destroy")
        for (arg in extraArguments) {
            destroyCommand = destroyCommand.withArgument(arg)
        }

        jenkinsfile = Jenkinsfile.instance

        return { ->
            node(jenkinsfile.getNodeName()) {
                deleteDir()
                checkout(scm)
                //properties([parameters(params)])

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
