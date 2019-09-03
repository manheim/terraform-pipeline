class ConsulBackendPlugin implements TerraformInitCommandPlugin {

    public static String defaultAddress
    public static Closure pathPattern

    public static void init() {
        ConsulBackendPlugin plugin = new ConsulBackendPlugin()

        TerraformInitCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformInitCommand command) {
        String environment = command.getEnvironment()
        String backendPath = getBackendPath(environment)
        command.withBackendConfig("path=${backendPath}")

        String consulAddress = getConsulAddress()
        if (consulAddress) {
            command.withBackendConfig("address=${consulAddress}")
        }
    }

    public String getBackendPath(String environment) {
        Closure backendPathPattern = pathPattern

        if (backendPathPattern == null)  {
            String repoSlug = Jenkinsfile.instance.getStandardizedRepoSlug()
            backendPathPattern = { String env -> "terraform/${repoSlug}_${env}" }
        }

        return backendPathPattern.call(environment)
    }

    public String getConsulAddress() {
        return defaultAddress ?: Jenkinsfile.instance.getEnv().DEFAULT_CONSUL_ADDRESS
    }
}
