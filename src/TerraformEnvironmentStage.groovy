import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class TerraformEnvironmentStage implements Stage {
    private Jenkinsfile jenkinsfile
    private String environment
    private StageDecorations decorations
    private TerraformInitCommand initCommand
    private TerraformPlanCommand planCommand
    private TerraformApplyCommand applyCommand
    private localPlugins

    private static final DEFAULT_PLUGINS = [ new ConditionalApplyPlugin(), new ConfirmApplyPlugin(), new DefaultEnvironmentPlugin() ]
    private static globalPlugins = DEFAULT_PLUGINS.clone()

    public static final String ALL = 'all'
    public static final String PLAN = 'plan'
    public static final String CONFIRM = 'confirm'
    public static final String APPLY = 'apply'

    TerraformEnvironmentStage(String environment) {
        this.environment = environment
        this.jenkinsfile = Jenkinsfile.instance
        this.decorations = new StageDecorations()
    }

    public Stage then(Stage nextStage) {
        return new BuildGraph(this).then(nextStage)
    }

    public static withGlobalEnv(String key, String value) {
        def plugin = new EnvironmentVariablePlugin()
        plugin.withEnv(key, value)
        addPlugin(plugin)

        return this
    }

    public TerraformEnvironmentStage withEnv(String key, String value) {
        def plugin = new EnvironmentVariablePlugin()
        plugin.withEnv(key, value)

        reconcileLocalAndGlobalPlugins(plugin)

        return this
    }

    public void build() {
        Jenkinsfile.build(pipelineConfiguration())
    }

    private Closure pipelineConfiguration() {
        initCommand = TerraformInitCommand.instanceFor(environment)
        planCommand = TerraformPlanCommand.instanceFor(environment)
        applyCommand = TerraformApplyCommand.instanceFor(environment)

        applyPlugins()

        def String environment = this.environment
        return { ->
            node(jenkinsfile.getNodeName()) {
                deleteDir()
                checkout(scm)

                decorations.apply(ALL) {
                    stage("${PLAN}-${environment}") {
                        decorations.apply(PLAN) {
                            sh initCommand.toString()
                            sh planCommand.toString()
                        }
                    }

                    decorations.apply("Around-${CONFIRM}") {
                        stage("${CONFIRM}-${environment}") {
                            decorations.apply(CONFIRM) {
                                echo "Approved"
                            }
                        }
                    }

                    decorations.apply("Around-${APPLY}") {
                        stage("${APPLY}-${environment}") {
                            decorations.apply(APPLY) {
                                sh initCommand.toString()
                                sh applyCommand.toString()
                            }
                        }
                    }
                }
            }
        }
    }

    public void decorate(Closure decoration) {
        decorations.add(ALL, decoration)
    }

    public decorate(String stageName, Closure decoration) {
        decorations.add(stageName, decoration)
    }

    public decorateAround(String stageName, Closure decoration) {
        decorations.add("Around-${stageName}", decoration)
    }

    public String toString() {
        return environment
    }

    public static addPlugin(plugin) {
        globalPlugins << plugin
    }

    public void applyPlugins() {
        // Apply both global and local plugins, in the correct order
        for (plugin in getAllPlugins()) {
            plugin.apply(this)
        }
    }

    public String getEnvironment() {
        return environment
    }

    /* Returns global globalPlugins, in addition to all
     * plugins that have been added to this instance
     */
    public getAllPlugins() {
        return reconcileLocalAndGlobalPlugins()
    }

    private reconcileLocalAndGlobalPlugins(TerraformEnvironmentStagePlugin newPlugin = null) {
        if (localPlugins == null) {
            if (newPlugin == null) {
                // No local plugins were added - only global plugins take effect
                return globalPlugins
            }

            // The first local plugin was added.  It takes effective *after* every current global plugin
            localPlugins = globalPlugins.clone()
            localPlugins << newPlugin
            return localPlugins
        }
        // We're here because a localPlugin was previously added.  Check if any new global plugins
        // have been added since.

        // Start off with all global plugins
        def remainingGlobalPlugins = globalPlugins.clone()
        for (def plugin in localPlugins) {
            // If all global plugins are accounted for, stop
            if (remainingGlobalPlugins.isEmpty()) {
                break;
            }
            // Cross off each global plugin that has not yet been accounted for
            if (remainingGlobalPlugins.first() == plugin) {
                remainingGlobalPlugins.remove(plugin)
            }
            // If the plugin was not in remainingGlobalPlugins, it means it was added locally
        }

        // Any global plugins that remain in this list have been added since the last time we checked.
        // Add them now.
        localPlugins.addAll(remainingGlobalPlugins)

        // If we have a new plugin to add, do it now
        if (newPlugin != null) {
            localPlugins << newPlugin
        }

        return localPlugins
    }

    public static getPlugins() {
        return globalPlugins
    }

    public static void resetPlugins() {
        this.globalPlugins = DEFAULT_PLUGINS.clone()
        // This totally jacks with localPlugins
    }

    public void createGithubComment(String issueNumber, String commentBody, String repoSlug, String credsID, String apiBaseUrl = 'http://github.ove.local/api/v3/') {
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
        //def tmpDir = pwd(tmp: true)
        //def bodyPath = "${tmpDir}/body.txt"
        //writeFile(file: bodyPath, text: data)
        def url = "${apiBaseUrl}repos/${repoSlug}/issues/${issueNumber}/comments"
        //echo "Creating comment in GitHub: ${data}"
        def output = null

        //withCredentials([$class: 'UsernamePasswordMultiBinding', credentialsId: credsID, usernameVariable: 'FOO', passwordVariable: 'GITHUB_TOKEN']) {
        //echo "\tRetrieved GITHUB_TOKEN from credential ${credsID}"
        def cmd = "curl -H \"Authorization: token \$GITHUB_TOKEN\" -X POST -d ${data} -H 'Content-Type: application/json' -D comment.headers ${url}"
        output = sh(script: cmd, returnStdout: true).trim()
        //}

        def headers = readFile('comment.headers').trim()
        if (! headers.contains('HTTP/1.1 201 Created')) {
            error("Creating GitHub comment failed: ${headers}\n${output}")
        }
        // ok, success
        def decoded = new JsonSlurper().parseText(output)
        //echo "Created comment ${decoded.id} - ${decoded.html_url}" 
        return
    }
}
