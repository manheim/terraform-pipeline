import com.manheim.releng.jenkins_pipeline_library.Utils

class TerraformPlanResultsPR implements TerraformPlanCommandPlugin {

    private landscape

    public static void init() {
        TerraformPlanResultsPR plugin = new TerraformPlanResultsPR()
        TerraformPlanCommandPlugin.addPlugin(plugin)
    }

    public static withLandscape(boolean landscape) {
        this.landscape = landscape
        return this
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        if (landscape) {
            command.withSuffix(" -out=tfplan -input=false 2>plan.err | landscape | tee plan.out")
        else{
            command.withSuffix(" -out=tfplan -input=false 2>plan.err | tee plan.out")
        }

        def repoHost = reutils.repoHost(reutils.shellOutput('git config remote.origin.url'))
        def repoSlug = reutils.repoSlug(reutils.shellOutput('git config remote.origin.url'))

        // comment on PR if this is a PR build
          if (env.BRANCH_NAME.startsWith("PR-")) {
            def prNum = env.BRANCH_NAME.replace('PR-', '')
            // this reads "plan.out" and strips the ANSI color escapes, which look awful in github markdown
            def planOutput = ''
            def planStderr = ''

            planOutput = readFile('plan.out').replaceAll(/\u001b\[[0-9;]+m/, '').replace(/^\[[0-9;]+m/, '')
            if(fileExists('plan.err')) {
                planStderr = readFile('plan.err').replaceAll(/\u001b\[[0-9;]+m/, '').replace(/^\[[0-9;]+m/, '').trim()
            }

            if(planStderr != '') {
              planOutput = planOutput + "\nSTDERR:\n" + planStderr
            }
            def commentBody = "Jenkins plan results ( ${env.BUILD_URL} ):\n\n" + '```' + "\n" + planOutput.trim() + "\n```" + "\n"
            reutils.createGithubComment(prNum, commentBody, repoSlug, 'man_releng', "https://${repoHost}/api/v3/")
          }

    }
}