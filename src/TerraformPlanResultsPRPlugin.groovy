import static TerraformEnvironmentStage.PLAN
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class TerraformPlanResultsPRPlugin implements TerraformPlanCommandPlugin, TerraformEnvironmentStagePlugin {

    private static String myRepoSlug
    private static String myRepoHost
    private static String githubTokenEnvVar = "GITHUB_TOKEN"

    public static void init() {
        TerraformPlanResultsPRPlugin plugin = new TerraformPlanResultsPRPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
    }

    public static withRepoSlug(String newRepoSlug) {
        TerraformPlanResultsPRPlugin.myRepoSlug = newRepoSlug
        return this
    }

    public static withRepoHost(String newRepoHost) {
        myRepoHost = newRepoHost
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
        command.withArgument("-out=tfplan")
        command.withStandardErrorRedirection('plan.err')
        command.withSuffix('| tee plan.out')
    }

    public Closure addComment(String env) {
        String build_url = Jenkinsfile.instance.getEnv().BUILD_URL

        return { closure ->
            closure()

            if (isPullRequest()) {
                def planOutput = getPlanOutput()
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

                    def url = getPullRequestCommentUrl()
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

    public String getRepoSlug() {
        if (myRepoSlug != null) {
            return myRepoSlug
        }

        def parsedScmUrl = Jenkinsfile.instance.getParsedScmUrl()
        def organization = parsedScmUrl['organization']
        def repo = parsedScmUrl['repo']

        return "${organization}/${repo}"
    }

    public String getRepoHost() {
        if (myRepoHost != null) {
            return myRepoHost
        }

        def parsedScmUrl = Jenkinsfile.instance.getParsedScmUrl()
        def protocol = parsedScmUrl['protocol']
        def domain = parsedScmUrl['domain']

        return "${protocol}://${domain}"
    }

    public String getBranchName() {
        return Jenkinsfile.instance.getEnv().BRANCH_NAME
    }

    public boolean isPullRequest() {
        def branchName = getBranchName()

        return branchName.startsWith('PR-')
    }

    public String getPullRequestNumber() {
        def branchName = getBranchName()

        return branchName.replace('PR-', '')
    }

    public String getPullRequestCommentUrl() {
        def repoHost = getRepoHost()
        def repoSlug = getRepoSlug()
        def pullRequestNumber = getPullRequestNumber()

        return "${repoHost}/api/v3/repos/${repoSlug}/issues/${pullRequestNumber}/comments".toString()
    }

    public String readFile(String filename) {
        def original = Jenkinsfile.instance.original
        if (original.fileExists(filename)) {
            return original.readFile(filename)
        }

        return null
    }

    public String getPlanOutput() {
        def planOutput =  readFile('plan.out')
        def planError = readFile('plan.err')
        // Skip any file outputs when the file does not exist
        def outputs = [planOutput, planError] - null

        // Strip any ANSI color encodings and whitespaces
        def results = outputs.collect { output ->
            output.replaceAll(/\u001b\[[0-9;]+m/, '')
                  .replace(/^\[[0-9;]+m/, '')
                  .trim()
        }

        // Separate by STDERR header if plan.err is not empty
        results.findAll { it != '' }
               .join('\nSTDERR:\n')
    }

    public static void reset() {
        myRepoSlug = null
        myRepoHost = null
    }
}
