import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.PLAN

class PlanOnlyStrategy {

    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private Jenkinsfile jenkinsfile

    public Closure createPipelineClosure(String environment, StageDecorations decorations, List params) {
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)

        jenkinsfile = Jenkinsfile.instance

        return { ->
            node(jenkinsfile.getNodeName()) {
                try {
                    deleteDir()
                    checkout(scm)
                    properties([parameters(params)])

                    decorations.apply(ALL) {
                        stage("${PLAN}-${environment}") {
                            decorations.apply(PLAN) {
                                sh initCommand.toString()
                                // sh planCommand.toString()
                                def status = sh(returnStatus: true, script: planCommand.toString())
                                sh "echo ${status}"
                            }
                        }
                    }
                    currentBuild.result = 'SUCCESS'
                } catch(Exception ex) {
                    echo "Caught exception: ${ex.toString()}"
                    currentBuild.result = 'FAILURE'
                    throw ex
                }
            }
        }
    }
}
