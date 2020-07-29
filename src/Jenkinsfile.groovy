class Jenkinsfile {
    public static original
    public static docker
    public static defaultNodeName
    public static repoSlug = null
    public static instance = new Jenkinsfile()
    public static declarative = false
    public static pipelineTemplate
    public static params = []

    def String getStandardizedRepoSlug() {
        if (repoSlug != null) {
            return repoSlug
        }

        def scmMap = getParsedScmUrl()
        repoSlug = "${standardizeString(scmMap['organization'])}/${standardizeString(scmMap['repo'])}"
        return repoSlug
    }

    def Map getParsedScmUrl() {
        def scmUrl = getScmUrl()
        return parseScmUrl(scmUrl)
    }

    def String getScmUrl() {
        def closure = {
            scm.getUserRemoteConfigs()[0].getUrl()
        }
        closure.delegate = original
        closure.call()
    }

    def Map parseScmUrl(String scmUrl) {
        def matcher = scmUrl =~ /(.*)(?::\/\/|\@)([^\/:]+)[\/:]([^\/]+)\/([^\/.]+)(.git)?/
        def Map results = [:]
        results.put("protocol", matcher[0][1])
        results.put("domain", matcher[0][2])
        results.put("organization", matcher[0][3])
        results.put("repo", matcher[0][4])
        return results
    }

    def String standardizeString(String original) {
        original.replaceAll( /-/, '_').replaceAll( /([A-Z]+)/, /_$1/ ).toLowerCase().replaceAll( /^_/, '' ).replaceAll( /_+/, '_')
    }

    def String getRepoName() {
        def Map scmMap = getParsedScmUrl()
        return scmMap['repo']
    }

    def String getOrganization() {
        def Map scmMap = getParsedScmUrl()
        return scmMap['organization']
    }
    
    public static List getParams() {
        return params
    }

    def static void init(original, Class customizations=null) {
        this.original = original
        this.docker   = original.docker

        initializeDefaultPlugins()

        if (customizations != null) {
            customizations.init()
        }
    }

    def static void initializeDefaultPlugins() {
        TerraformPlugin.init()
    }

    def static String getNodeName() {
        return defaultNodeName ?: instance.getEnv().DEFAULT_NODE_NAME
    }

    public static build(Closure closure) {
        original.ApplyJenkinsfileClosure(closure)
    }

    public static void build(List<Stage> stages) {
        def param_closure = { closure ->
            properties([parameters(this.params)])
        }
        original.ApplyJenkinsfileClosure(param_closure)

        if (!declarative) {
            stages.each { Stage stage -> stage.build() }
        } else {
            if (pipelineTemplate == null) {
                this.pipelineTemplate = getPipelineTemplate(stages)
            }

            pipelineTemplate.call(stages)
        }
    }

    public static getPipelineTemplate(List<Stage> stages) {
        switch (stages.size()) {
            case 2:
                return original.Pipeline2Stage
            case 3:
                return original.Pipeline3Stage
            case 4:
                return original.Pipeline4Stage
            case 5:
                return original.Pipeline5Stage
            case 6:
                return original.Pipeline6Stage
            case 7:
                return original.Pipeline7Stage
        }

        throw new RuntimeException("Your pipeline has ${stages.size()} stages - the maximum supported by default is 7.  Define a custom pipeline template and assign it to Jenkinsfile.pipelineTemplate to create your pipeline.")
    }

    public getEnv() {
        return original.env
    }

    public static withInstance(Jenkinsfile newInstance) {
        this.instance = newInstance
    }

    public static void addParam(newParam) {
        this.params << newParam
    }

    public static reset() {
        instance = new Jenkinsfile()
        original = null
        params = []
    }
}
