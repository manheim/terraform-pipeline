class AwssumePlugin implements TerraformInitCommandPlugin, TerraformPlanCommandPlugin, TerraformApplyCommandPlugin {
    public static void init() {
        AwssumePlugin plugin = new AwssumePlugin()

        TerraformInitCommand.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformInitCommand command) {
        applyToCommand(command)
    }

    @Override
    public void apply(TerraformPlanCommand command) {
        applyToCommand(command)
    }

    @Override
    public void apply(TerraformApplyCommand command) {
        applyToCommand(command)
    }

    // We lost type safety here - we should have a TerraformCommand interface
    private void applyToCommand(command) {
        String environment = command.getEnvironment()
        String region = getRegion(environment)
        String iamArn = getAwsRoleArn(environment)

        if (iamArn) {
            command.withPrefix("AWS_REGION=${region} AWS_ROLE_ARN=${iamArn} awssume")
        } else {
            println("No AWS_ROLE_ARN is set, so awssume will not be used for terraform in the ${environment} environment.")
        }
    }

    public String getAwsRoleArn(String environment) {
        String role = Jenkinsfile.instance.getEnv()['AWS_ROLE_ARN']

        if (role == null) {
            role = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_AWS_ROLE_ARN"]
        }

        if (role == null) {
            role = Jenkinsfile.instance.getEnv()["${environment}_AWS_ROLE_ARN"]
        }

        return role
    }

    public String getRegion(String environment) {
        String region = Jenkinsfile.instance.getEnv()['AWS_REGION']

        if (region == null) {
            region = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_AWS_REGION"]
        }

        if (region == null) {
            region = Jenkinsfile.instance.getEnv()['AWS_DEFAULT_REGION']
        }

        return region
    }
}
