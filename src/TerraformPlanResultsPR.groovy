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
                //createGithubComment(prNum, commentBody, repoSlug, 'man_releng', "https://${repoHost}/api/v3/")
            }
        }
    }

    public void createGithubComment(String issueNumber, String commentBody, String repoSlug, String credsID, String apiBaseUrl = 'https://ghe.coxautoinc.com/api/v3/') {
        sh "echo ${issueNumber}"
        sh "echo ${commentBody}"
        sh "echo ${repoSlug}"
        sh "echo ${credsID}"
        sh "echo ${apiBaseUrl}"

        //echo("Creating comment in GitHub")


        //def maxlen = 65535
        //def textlen = commentBody.length()
        //def chunk = ""
        //if (textlen > maxlen) {
            // GitHub can't handle comments of 65536 or longer; chunk
        //    def result = null
        //    def i = 0
        //    for (i = 0; i < textlen; i += maxlen) {
        //        chunk = commentBody.substring(i, Math.min(textlen, i + maxlen))
        //        result = createGithubComment(issueNumber, chunk, repoSlug, credsID, apiBaseUrl)
        //    }
        //    return result
        //}
        //def data = JsonOutput.toJson([body: commentBody])
        //def tmpDir = pwd(tmp: true)
        //def bodyPath = "${tmpDir}/body.txt"
        //writeFile(file: bodyPath, text: data)
        //def url = "${apiBaseUrl}repos/${repoSlug}/issues/${issueNumber}/comments"
        //sh "echo 'Creating comment in GitHub: ${data}'"
        //def output = null
        //withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credsID, usernameVariable: 'FOO', passwordVariable: 'GITHUB_TOKEN']]) {
        //    sh "echo '\tRetrieved GITHUB_TOKEN from credential ${credsID}'"
        //    def cmd = "curl -H \"Authorization: token \$GITHUB_TOKEN\" -X POST -d @${bodyPath} -H 'Content-Type: application/json' -D comment.headers ${url}"
        //   output = sh(script: cmd, returnStdout: true).trim()
        //}
        //def headers = readFile('comment.headers').trim()
        //if (! headers.contains('HTTP/1.1 201 Created')) {
        //    error("Creating GitHub comment failed: ${headers}\n${output}")
        //}
        // ok, success
        //def decoded = new JsonSlurper().parseText(output)
        //echo("Created comment ${decoded.id} - ${decoded.html_url}")
        return
    }

}