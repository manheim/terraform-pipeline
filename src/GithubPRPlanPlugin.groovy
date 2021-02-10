import static TerraformEnvironmentStage.PLAN
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class GithubPRPlanPlugin implements TerraformPlanCommandPlugin, TerraformEnvironmentStagePlugin, Resettable {

    private static String myRepoSlug
    private static String myRepoHost
    private static String githubTokenEnvVar = "GITHUB_TOKEN"
    private static final int MAX_COMMENT_LENGTH = 65535

    public static void init() {
        GithubPRPlanPlugin plugin = new GithubPRPlanPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
    }

    public static withRepoSlug(String newRepoSlug) {
        GithubPRPlanPlugin.myRepoSlug = newRepoSlug
        return this
    }

    public static withRepoHost(String newRepoHost) {
        myRepoHost = newRepoHost
        return this
    }

    public static withGithubTokenEnvVar(String githubTokenEnvVar) {
        GithubPRPlanPlugin.githubTokenEnvVar = githubTokenEnvVar
        return this
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(PLAN, addComment(stage.getEnvironment()))
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        command.withPrefix("set -o pipefail;")
        command.withArgument("-out=tfplan")
        command.withStandardErrorRedirection('plan.err')
        command.withSuffix('| tee plan.out')
    }

    public Closure addComment(String env) {
        return { closure ->
            closure()

            if (isPullRequest()) {
                String url = getPullRequestCommentUrl()
                String comment = getCommentBody(env)
                postPullRequestComment(url, comment)
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

        // We cannot post using the git protocol, change to https
        if (protocol == "git") {
            protocol = "https"
        }

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

    public String getBuildResult() {
        Jenkinsfile.instance.original.currentBuild.currentResult
    }

    public String getBuildUrl() {
        Jenkinsfile.instance.original.build_url
    }

    public String getCommentBody(String environment) {
        def planOutput = getPlanOutput()
        def buildResult = getBuildResult()
        def buildUrl = getBuildUrl()
        def lines = []
        lines << "**Jenkins plan results for ${environment}** - ${buildResult} ( ${buildUrl} ):"
        lines << ''
        lines << '```'
        lines << planOutput
        lines << '```'
        lines << ''

        return lines.join('\n')
    }

    public postPullRequestComment(String pullRequestUrl, String commentBody) {
        def closure = { ->
            echo "Creating comment in GitHub"
            // GitHub can't handle comments of 65536 or longer; chunk
            commentBody.split("(?<=\\G.{${MAX_COMMENT_LENGTH}})").each { chunk ->
                def data = JsonOutput.toJson([body: chunk])
                def tmpDir = pwd(tmp: true)
                def bodyPath = "${tmpDir}/body.txt"
                writeFile(file: bodyPath, text: data)

                def cmd = "curl -H \"Authorization: token \$${githubTokenEnvVar}\" -X POST -d @${bodyPath} -H 'Content-Type: application/json' -D comment.headers ${pullRequestUrl}"

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

        closure.delegate = Jenkinsfile.original
        closure()
    }

    public static void reset() {
        myRepoSlug = null
        myRepoHost = null
    }
}
