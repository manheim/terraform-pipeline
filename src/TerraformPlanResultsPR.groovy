import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import static TerraformEnvironmentStage.PLAN

class TerraformPlanResultsPR implements TerraformPlanCommandPlugin, TerraformEnvironmentStagePlugin {

    private static boolean landscape = false
    private static String repoSlug = ""

    public static void init() {
        TerraformPlanResultsPR plugin = new TerraformPlanResultsPR()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
    }

    public static withLandscape(boolean landscape) {
        TerraformPlanResultsPR.landscape = landscape
        return this
    }

    public static withRepoSlug(String repoSlug){
        TerraformPlanResultsPR.repoSlug = repoSlug
        return this
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN, addComment())
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        if (landscape) {
            command.withSuffix(" -out=tfplan -input=false 2>plan.err | landscape | tee plan.out")
        }
        else {
            command.withSuffix(" -out=tfplan -input=false 2>plan.err | tee plan.out")
        }
    }

    public static Closure addComment() {
        String repoHost = "ghe.coxautoinc.com"
        String branch = Jenkinsfile.instance.getEnv().BRANCH_NAME
        String build_url = Jenkinsfile.instance.getEnv().BUILD_URL
        
        return { closure -> 
            closure()
            sh "echo ${branch}"
            sh "echo ${build_url}"

            // comment on PR if this is a PR build
            if (branch.startsWith("PR-")) {
                String prNum = branch.replace('PR-', '')
                // this reads "plan.out" and strips the ANSI color escapes, which look awful in github markdown
                String planOutput = ''
                String planStderr = ''

                planOutput = readFile('plan.out').replaceAll(/\u001b\[[0-9;]+m/, '').replace(/^\[[0-9;]+m/, '')
                if (fileExists('plan.err')) {
                    planStderr = readFile('plan.err').replaceAll(/\u001b\[[0-9;]+m/, '').replace(/^\[[0-9;]+m/, '').trim()
                }

                if (planStderr != '') {
                    planOutput = planOutput + "\nSTDERR:\n" + planStderr
                }
                String commentBody = "Jenkins plan results ( ${build_url} ):\n\n" + '```' + "\n" + planOutput.trim() + "\n```" + "\n"

                sh "echo Creating comment in GitHub"
                createGithubComment(prNum, commentBody, repoSlug, 'man_releng', "https://${repoHost}/api/v3/")

            }
        }
    }

    public void createGithubComment(String issueNumber, String commentBody, String repoSlug, String credsID, String apiBaseUrl = 'http://github.ove.local/api/v3/') {
        def maxlen = 65535
        def textlen = commentBody.length()
    }
}