import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.APPLY

class PassPlanFilePlugin implements TerraformPlanCommandPlugin, TerraformApplyCommandPlugin, TerraformEnvironmentStagePlugin {

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
            echo "Archiving tfplan-${env} file"
            archiveArtifacts artifacts: "tfplan-" + env
        }
    }

    public Closure downloadArchive(String env) {
        return { closure ->
            String jenkinsUrl  = Jenkinsfile.instance.getEnv()['JENKINS_URL']
            String jobName     = Jenkinsfile.instance.getEnv()['JOB_NAME']
            String buildNumber = Jenkinsfile.instance.getEnv()['BUILD_NUMBER']
            String url = getArtifactUrl(jenkinsUrl, jobName, buildNumber, env)
            echo "Downloading tfplan-${env} file from ${url}"

            sh "wget -O tfplan-${env} ${url}"

            closure()
        }
    }

    public String getArtifactUrl(String jenkinsUrl, String jobName, String buildNumber, String env) {
        String[] jobNameArr = jobName.split("/")

        String newJobName = ""
        for (int i = 0; i < jobNameArr.length; i++) {
            if (i == jobNameArr.length - 1) { // Last element
                newJobName += jobNameArr[i]
            }
            else {
                newJobName += jobNameArr[i] + "/job/"
            }
        }

        String url = jenkinsUrl + "job/" + newJobName + "/" + buildNumber + "/artifact/tfplan-" + env
        return url
    }

}
