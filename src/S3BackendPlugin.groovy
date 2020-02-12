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
            String repoSlug = getStandardizedRepoSlug()
            backendKeyPattern = { String env -> "terraform/${repoSlug}/${env}" }
        }

        return backendKeyPattern.call(environment)
    }

    public String getBucket(String environment) {
        def env = getEnv()
        String bucket = env["S3_BACKEND_BUCKET"]

        if (bucket == null) {
            println("No S3_BACKEND_BUCKET found - checking for environment-specific bucket")
            bucket = env["${environment.toUpperCase()}_S3_BACKEND_BUCKET"]
        }

        if (bucket == null) {
            bucket = env["${environment}_S3_BACKEND_BUCKET"]
        }

        if (bucket == null) {
            println("No ${environment.toUpperCase()}_S3_BACKEND_BUCKET found either.")
        }

        return bucket
    }

    public String getRegion(String environment) {
        def env = getEnv()
        String region = env['S3_BACKEND_REGION']

        if (region == null) {
            println("No S3_BACKEND_REGION found - checking for environment-specific region")
            region = env["${environment.toUpperCase()}_S3_BACKEND_REGION"]
        }

        if (region == null) {
            region = env["${environment}_S3_BACKEND_REGION"]
        }

        if (region == null) {
            region = env['DEFAULT_S3_BACKEND_REGION']
            if (region != null) {
                println("WARNING: DEFAULT_S3_BACKEND_REGION is deprecated, please use S3_BACKEND_REGION or ${environment.toUpperCase()}_S3_BACKEND_REGION")
            }
        }

        return region
    }

    public String getDynamodbTable(String environment) {
        def env = getEnv()
        String table = env["S3_BACKEND_DYNAMODB_TABLE"]

        if (table == null) {
            table = env["${environment.toUpperCase()}_S3_BACKEND_DYNAMODB_TABLE"]
        }

        if (table == null) {
            table = env["${environment}_S3_BACKEND_DYNAMODB_TABLE"]
        }

        if (table == null) {
            table = env["${environment.toUpperCase()}_S3_BACKEND_DYNAMO_TABLE_LOCK"]
            if (table != null) {
                println("${environment.toUpperCase()}_S3_BACKEND_DYNAMO_TABLE_LOCK is deprecated - please use ${environment.toUpperCase()}_S3_BACKEND_DYNAMODB_TABLE instead")
            }
        }

        return table
    }

    public String getEncrypt(String environment) {
        def env = getEnv()
        String encrypt = env["S3_BACKEND_ENCRYPT"]

        if (encrypt == null) {
            encrypt = env["${environment.toUpperCase()}_S3_BACKEND_ENCRYPT"]
        }

        if (encrypt == null) {
            encrypt = env["${environment}_S3_BACKEND_ENCRYPT"]
        }

        return encrypt
    }

    public String getKmsKeyId(String environment) {
        def env = getEnv()
        String arn = env["S3_BACKEND_KMS_KEY_ID"]

        if (arn == null) {
            arn = env["${environment.toUpperCase()}_S3_BACKEND_KMS_KEY_ID"]
        }

        if (arn == null) {
            arn = env["${environment}_S3_BACKEND_KMS_KEY_ID"]
        }

        return arn
    }

    public getEnv() {
        return (Jenkinsfile.instance != null) ? Jenkinsfile.instance.getEnv() : [:]
    }

    public getStandardizedRepoSlug() {
        return (Jenkinsfile.instance != null) ? Jenkinsfile.instance.getStandardizedRepoSlug() : null
    }
}
