import static TerraformEnvironmentStage.PLAN
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class TerraformPlanResultsPRPlugin implements TerraformPlanCommandPlugin, TerraformEnvironmentStagePlugin {

    private static boolean landscape = false
    private static String repoSlug = ""
    private static String repoHost = "https://api.github.com/"
    private static String githubTokenEnvVar = "GITHUB_TOKEN"

    public static void init() {
        TerraformPlanResultsPRPlugin plugin = new TerraformPlanResultsPRPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
    }

    public static withLandscape(boolean landscape) {
        TerraformPlanResultsPRPlugin.landscape = landscape
        return this
    }

    public static withRepoSlug(String repoSlug) {
        TerraformPlanResultsPRPlugin.repoSlug = repoSlug
        return this
    }

    public static withRepoHost(String repoHost) {
        TerraformPlanResultsPRPlugin.repoHost = repoHost
        return this
    }

    public static withGithubTokenEnvVar(String githubTokenEnvVar) {
        TerraformPlanResultsPRPlugin.githubTokenEnvVar = githubTokenEnvVar
        return this
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN, addComment(stage.getEnvironment()))
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        if (landscape) {
            command.withSuffix(" -out=tfplan 2>plan.err | landscape | tee plan.out")
        }
        else {
            command.withSuffix(" -out=tfplan 2>plan.err | tee plan.out")
        }
    }

    public static Closure addComment(String env) {
        String branch = Jenkinsfile.instance.getEnv().BRANCH_NAME
        String build_url = Jenkinsfile.instance.getEnv().BUILD_URL

        return { closure ->
            closure()

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
                String commentBody = "**Jenkins plan results for ${env}** - ${currentBuild.currentResult} ( ${build_url} ):\n\n" + '```' + "\n" + planOutput.trim() + "\n```" + "\n"
                echo "Creating comment in GitHub"
                def maxlen = 65535
                def textlen = commentBody.length()
                def chunk = "" // GitHub can't handle comments of 65536 or longer; chunk
                def i = 0
                for (i = 0; i < textlen; i += maxlen) {
                    chunk = commentBody.substring(i, Math.min(textlen, i + maxlen))

                    def data = JsonOutput.toJson([body: chunk])
                    def tmpDir = pwd(tmp: true)
                    def bodyPath = "${tmpDir}/body.txt"
                    writeFile(file: bodyPath, text: data)

                    def url = "${repoHost}repos/${repoSlug}/issues/${prNum}/comments"
                    def cmd = "curl -H \"Authorization: token \$${githubTokenEnvVar}\" -X POST -d @${bodyPath} -H 'Content-Type: application/json' -D comment.headers ${url}"

                    def output = sh(script: cmd, returnStdout: true).trim()

                    def headers = readFile('comment.headers').trim()
                    if (! headers.contains('HTTP/1.1 201 Created')) {
                        error("Creating GitHub comment failed: ${headers}\n")
                    }
                    // ok, success
                    def decoded = new JsonSlurper().parseText(output)
                    echo "Created comment ${decoded.id} - ${decoded.html_url}"
                }
            }
        }
    }
}
