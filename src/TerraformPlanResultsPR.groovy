import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import static TerraformEnvironmentStage.CONFIRM

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
        stage.decorate(CONFIRM, addComment())
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
        return { closure -> 
            sh "echo HERE#########"
            closure()
        }
    }

    public void createGithubComment(String issueNumber, String commentBody, String repoSlug, String credsID, String apiBaseUrl = 'https://ghe.coxautoinc.com/api/v3/') {
        def maxlen = 65535
        def textlen = commentBody.length()
        def chunk = ""
        if (textlen > maxlen) {
            // GitHub can't handle comments of 65536 or longer; chunk
            def result = null
            def i = 0
            for (i = 0; i < textlen; i += maxlen) {
                chunk = commentBody.substring(i, Math.min(textlen, i + maxlen))
                result = createGithubComment(issueNumber, chunk, repoSlug, credsID, apiBaseUrl)
            }
            return result
        }
        def data = JsonOutput.toJson([body: commentBody])
        def tmpDir = steps.pwd(tmp: true)
        def bodyPath = "${tmpDir}/body.txt"
        steps.writeFile(file: bodyPath, text: data)
        def url = "${apiBaseUrl}repos/${repoSlug}/issues/${issueNumber}/comments"
        steps.echo("Creating comment in GitHub: ${data}")
        def output = null
        steps.withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credsID, usernameVariable: 'FOO', passwordVariable: 'GITHUB_TOKEN']]) {
            steps.echo("\tRetrieved GITHUB_TOKEN from credential ${credsID}")
            def cmd = "curl -H \"Authorization: token \$GITHUB_TOKEN\" -X POST -d @${bodyPath} -H 'Content-Type: application/json' -D comment.headers ${url}"
            output = steps.sh(script: cmd, returnStdout: true).trim()
        }
        def headers = steps.readFile('comment.headers').trim()
        if (! headers.contains('HTTP/1.1 201 Created')) {
            steps.error("Creating GitHub comment failed: ${headers}\n${output}")
        }
        // ok, success
        def decoded = new JsonSlurper().parseText(output)
        steps.echo("Created comment ${decoded.id} - ${decoded.html_url}")
        return
    }

    public boolean fileExists(String filename) {
        return getJenkinsOriginal().fileExists(filename)
    }

    public String readFile(String filename) {
        def content = (getJenkinsOriginal().readFile(filename) as String)
        return content.trim()
    }

    public getJenkinsOriginal() {
        return  Jenkinsfile.instance.original
    }

}