import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.APPLY
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class PassPlanFilePlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin, TerraformEnvironmentStagePlugin {

    private static String planAbsolutePath

    public static void init() {
        PassPlanFilePlugin plugin = new PassPlanFilePlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN,  archivePlanFile(stage.getEnvironment()))
        stage.decorate(APPLY, downloadArchive(stage.getEnvironment()))
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        String env = command.getEnvironment()
        command.withArgument("-out=tfplan-" + env)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        String env = command.getEnvironment()
        command.withArgument("tfplan-" + env)
    }

    public Closure archivePlanFile(String env) {
        return { closure ->
            closure()
            String workingDir = pwd()
            String planFileName = workingDir + "/tfplan-" + env
            archiveArtifacts artifacts: "tfplan-" + env
            setAbsolutePath(planFileName)
        }
    }

    public Closure downloadArchive(String env) {
        return { closure ->
            String jenkinsUrl  = Jenkinsfile.instance.getEnv()['JENKINS_URL']

            String jobName     = Jenkinsfile.instance.getEnv()['JOB_NAME']

            String newJobName = ""
            for (String part : jobName.split("/"))
                newJobName += part + "/job/"

            echo newJobName

            String branch      = Jenkinsfile.instance.getEnv()['BRANCH_NAME']
            String buildNumber = Jenkinsfile.instance.getEnv()['BUILD_NUMBER']

            String url = jenkinsUrl + "job/" + jobName + "/" + buildNumber + "/artifact/tfplan-" + env
            echo url
            sh "wget ${url}"

            closure()

        }
    }

    public void setAbsolutePath(String planFileName) {
        this.planAbsolutePath = planFileName
    }


    public static void reset() {
        planAbsolutePath = null
    }
}
