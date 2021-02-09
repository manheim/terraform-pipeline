import static TerraformEnvironmentStage.ALL

class WithAwsPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    private static role

    public static void init() {
        WithAwsPlugin plugin = new WithAwsPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        String environment = stage.getEnvironment()

        stage.decorate(ALL, addWithAwsRole(environment))
    }

    public Closure addWithAwsRole(String environment) {
        return { closure ->
            String iamRole = getRole(environment)

            if (iamRole != null) {
                withAWS(role: iamRole) {
                    sh "echo Running AWS commands under the role: ${iamRole}"
                    closure()
                }
            } else {
                sh "echo no role found. Skipping withAWS"
                closure()
            }
        }
    }

    public static withRole(String role = null) {
        this.role = role

        return this
    }

    public String getRole(String environment) {
        def tempRole = this.role

        if (tempRole == null) {
            tempRole = Jenkinsfile.instance.getEnv()['AWS_ROLE_ARN']
        }

        if (tempRole == null) {
            tempRole = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_AWS_ROLE_ARN"]
        }

        if (tempRole == null) {
            tempRole = Jenkinsfile.instance.getEnv()["${environment}_AWS_ROLE_ARN"]
        }

        return tempRole
    }

    public static void reset() {
        this.role = null
    }
}
