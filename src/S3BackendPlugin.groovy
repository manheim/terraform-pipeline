class S3BackendPlugin implements TerraformInitCommandPlugin {

    public static Closure keyPattern

    public static void init() {
        S3BackendPlugin plugin = new S3BackendPlugin()

        TerraformInitCommand.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformInitCommand command) {
        String environment = command.getEnvironment()

        def configs = [:]
        configs['key'] = getKey(environment)
        configs['bucket'] = getBucket(environment)
        configs['region'] = getRegion(environment)
        configs['dynamodb_table'] = getDynamodbTable(environment)
        configs['encrypt'] = getEncrypt(environment)
        configs['kms_key_id'] = getKmsKeyId(environment)


        configs.each { entry ->
            if (entry.value?.trim()) {
                command.withBackendConfig("${entry.key}=${entry.value}")
            } else {
                println("No S3 backend ${entry.key} found")
            }
        }
    }

    public String getKey(String environment) {
        Closure backendKeyPattern = keyPattern

        if (backendKeyPattern == null)  {
            String repoSlug = Jenkinsfile.instance.getStandardizedRepoSlug()
            backendKeyPattern = { String env -> "terraform/${repoSlug}/${env}" }
        }

        return backendKeyPattern.call(environment)
    }

    public String getBucket(String environment) {
        String bucket = Jenkinsfile.instance.getEnv()["S3_BACKEND_BUCKET"]

        if (bucket == null) {
            println("No S3_BACKEND_BUCKET found - checking for environment-specific bucket")
            bucket = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_S3_BACKEND_BUCKET"]
        }

        if (bucket == null) {
            bucket = Jenkinsfile.instance.getEnv()["${environment}_S3_BACKEND_BUCKET"]
        }

        if (bucket == null) {
            println("No ${environment.toUpperCase()}_S3_BACKEND_BUCKET found either.")
        }

        return bucket
    }

    public String getRegion(String environment) {
        String region = Jenkinsfile.instance.getEnv()['S3_BACKEND_REGION']

        if (region == null) {
           println("No S3_BACKEND_REGION found - checking for environment-specific region")
           region = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_S3_BACKEND_REGION"]
        }

        if (region == null) {
           region = Jenkinsfile.instance.getEnv()["${environment}_S3_BACKEND_REGION"]
        }

        if (region == null) {
            region = Jenkinsfile.instance.getEnv()['DEFAULT_S3_BACKEND_REGION']
            if (region != null) {
                println("WARNING: DEFAULT_S3_BACKEND_REGION is deprecated, please use S3_BACKEND_REGION or ${environment.toUpperCase()}_S3_BACKEND_REGION")
            }
        }

        return region
    }

    public String getDynamodbTable(String environment) {
        String table = Jenkinsfile.instance.getEnv()["S3_BACKEND_DYNAMODB_TABLE"]

        if (table == null) {
            table = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_S3_BACKEND_DYNAMODB_TABLE"]
        }

        if (table == null) {
            table = Jenkinsfile.instance.getEnv()["${environment}_S3_BACKEND_DYNAMODB_TABLE"]
        }

        if (table == null) {
            table = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_S3_BACKEND_DYNAMO_TABLE_LOCK"]
            if (table != null) {
                println("${environment.toUpperCase()}_S3_BACKEND_DYNAMO_TABLE_LOCK is deprecated - please use ${environment.toUpperCase()}_S3_BACKEND_DYNAMODB_TABLE instead")
            }
        }

        return table
    }

    public String getEncrypt(String environment) {
        String encrypt = Jenkinsfile.instance.getEnv()["S3_BACKEND_ENCRYPT"]

        if (encrypt == null) {
            encrypt = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_S3_BACKEND_ENCRYPT"]
        }

        if (encrypt == null) {
            encrypt = Jenkinsfile.instance.getEnv()["${environment}_S3_BACKEND_ENCRYPT"]
        }

        return encrypt
    }

    public String getKmsKeyId(String environment) {
        String arn = Jenkinsfile.instance.getEnv()["S3_BACKEND_KMS_KEY_ID"]

        if (arn == null) {
            arn = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_S3_BACKEND_KMS_KEY_ID"]
        }

        if (arn == null) {
            arn = Jenkinsfile.instance.getEnv()["${environment}_S3_BACKEND_KMS_KEY_ID"]
        }

        return arn
    }
}
