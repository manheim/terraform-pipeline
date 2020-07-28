import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.PLAN

class PlanOnlyStrategy {

    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private Jenkinsfile jenkinsfile
    //private boolean fail_plan_on_changes = false

    public Closure createPipelineClosure(String environment, StageDecorations decorations, List params) {
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        
        //fail_plan_on_changes = Jenkinsfile.instance.getEnv().FAIL_PLAN_ON_CHANGES ?: false
        if (Jenkinsfile.instance.getEnv().FAIL_PLAN_ON_CHANGES == true) {
            planCommand = planCommand.withArgument('-detailed-exitcode')
        }

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
                            sh returnStatus: true, script: planCommand.toString()
                        }
                    }
                }
            }
        }
    }
}
