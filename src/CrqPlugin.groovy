class CrqPlugin implements TerraformEnvironmentStagePlugin {
    public static defaultBau = 152

    public static void init() {
        TerraformEnvironmentStage.addPlugin(new CrqPlugin())
    }

    CrqPlugin() {
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        def environment = stage.getEnvironment()

        stage.decorate(TerraformEnvironmentStage.APPLY, addCrq(environment))
    }

    public String getCrqEnvironment(String environment) {
        def env = getEnv()
        String crqEnvironment = env['CRQ_ENVIRONMENT']

        if (crqEnvironment == null) {
            crqEnvironment = env["${environment.toUpperCase()}_CRQ_ENVIRONMENT"]
        }

        return crqEnvironment
    }

    public Closure addCrq(String environment) {
        return { closure ->
            def crqEnvironment = getCrqEnvironment(environment)

            if (crqEnvironment) {
                def config = [
                    environment: environment,
                    app: Jenkinsfile.instance.getRepoName(),
                    crqEnvironment: crqEnvironment
                ]

                sh "${remedierOpen(config)}"
                try {
                    closure()
                    sh "${remedierClose(config)}"
                } catch (err) {
                    sh "${remedierBackout(config)}"
                    throw err
                }
            } else {
                sh "echo No CRQ_ENVIRONMENT found, set this to trigger automated CRQs"
                closure()
            }
        }
    }

    public static String remedierOpen(config = [:]) {
        def app = config.app ?: "\$APP"
        def bau = config.bau ?: defaultBau
        def environment = config.environment ?: "\$ENVIRONMENT"
        def crqEnvironment = config.crqEnvironment ?: '$CRQ_ENVIRONMENT'
        def summary     = config.summary ?: "${app} - Deploy - ${environment}"
        def productName = config.productName ?: "Software Delivery Pipeline"
        def firstName   = config.firstName ?: "\$DEFAULT_PIPELINE_CRQ_FIRST_NAME"
        def lastName    = config.lastName ?: "\$DEFAULT_PIPELINE_CRQ_LAST_NAME"
        def login       = config.login ?: "\$DEFAULT_PIPELINE_CRQ_LOGIN"
        def tier1       = config.tier1 ?: "Software"
        def tier2       = config.tier2 ?: "Application"
        def tier3       = config.tier3 ?: "Release Management"

        def message = "See \$BUILD_URL"
        return "manheim_remedy open \"${bau}\" \"${productName}\" \"${firstName}\" \"${lastName}\" \"${login}\" \"${tier1}\" \"${tier2}\" \"${tier3}\" \"${summary}\" \"${crqEnvironment}\" \"${message}\""
    }

    public static String remedierClose(config = [:]) {
        return "manheim_remedy close `cat ChangeID.txt | sed 's/ChangeID=//g'`"
    }

    public static String remedierBackout(config = [:]) {
        def reason = "Change failed"
        return "manheim_remedy error `cat ChangeID.txt | sed 's/ChangeID=//g'` \"${reason}\""
    }

    public getEnv() {
        return Jenkinsfile.instance.getEnv()
    }

}
