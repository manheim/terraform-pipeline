import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.MatcherAssert.assertThat

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class S3BackendPluginTest {
    @Nested
    public class Init {
        @Test
        void addsS3BackendPluginToListOfPlugins() {
            S3BackendPlugin.init()

            Collection actualPlugins = TerraformInitCommand.getPlugins()
            assertThat(actualPlugins, contains(instanceOf(S3BackendPlugin.class)))
        }
    }

    @Nested
    public class Apply {
        @Test
        void addsEnvironmentSpecificKeyAsBackendParameter() {
            String repoSlug = 'myOrg/myRepo'
            String environment = "myEnv"
            MockJenkinsfile.withStandardizedRepoSlug(repoSlug).withEnv()
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=key=terraform/${repoSlug}/${environment}"))
        }

        @Test
        void addsBucketPreDefinedByEnvironmentAsBackendParameter() {
            String expectedBucket = 'bucket'
            String environment = "myEnv"
            MockJenkinsfile.withEnv(MYENV_S3_BACKEND_BUCKET: expectedBucket)
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=bucket=${expectedBucket}"))
        }

        @Test
        void addsBucketRegionUsingPreDefinedEnvironmentVariableAsBackendParameter() {
            String expectedRegion = 'theFarEast'

            String environment = "myEnv"
            MockJenkinsfile.withEnv('DEFAULT_S3_BACKEND_REGION': expectedRegion)
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=region=${expectedRegion}"))
        }

        @Test
        void addsDynamoDbTableAsBackendParameter() {
            String dynamodb_table = 'terraform-state-lock-dynamo'

            String environment = "myEnv"
            MockJenkinsfile.withEnv('MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK': dynamodb_table)
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=dynamodb_table=${dynamodb_table}"))
        }

        @Test
        void skipsDynamoDbTableAsBackendParameterWhenNoneSpecified() {
            String environment = "myEnv"
            S3BackendPlugin plugin = new S3BackendPlugin()
            MockJenkinsfile.withEnv()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-backend-config=dynamodb_table=")))
        }

        @Test
        void isAddedAndUsesCustomizedPatternFolderKeyAsBackendParameter() {
            String repoSlug = 'myOrg/myRepo'
            S3BackendPlugin.keyPattern = { String env -> "customPatternFor/${repoSlug}/entropy/${env}" }
            MockJenkinsfile.withStandardizedRepoSlug(repoSlug).withEnv()
            S3BackendPlugin plugin = new S3BackendPlugin()

            String environment = "myEnv"
            TerraformInitCommand command = new TerraformInitCommand(environment)
            plugin.apply(command)
            assertThat(command.toString(), containsString("-backend-config=key=customPatternFor/${repoSlug}/entropy/${environment}"))
        }
    }

    @Nested
    public class GetBackend {
        @Test
        void shouldReturnTheValueOfS3BackendBucket() {
            String expectedBucket = 'defaultBucket'
            MockJenkinsfile.withEnv('S3_BACKEND_BUCKET': expectedBucket)
            def plugin = new S3BackendPlugin()

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldReturnTheValueOfTheEnvironmentSpecificS3BackendBucket() {
            String expectedBucket = 'myBucket'
            MockJenkinsfile.withEnv('MYENV_S3_BACKEND_BUCKET': expectedBucket)
            def plugin = new S3BackendPlugin()

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldReturnTheValueOfTheEnvironmentSpecificS3BackendBucketCaseInsensitive() {
            String expectedBucket = 'myBucket'
            MockJenkinsfile.withEnv('myenv_S3_BACKEND_BUCKET': expectedBucket)
            def plugin = new S3BackendPlugin()

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldPreferS3BackendBucketOverEnvironmentSpecificBucket() {
            String expectedBucket = "thisBucket"
            MockJenkinsfile.withEnv('S3_BACKEND_BUCKET': expectedBucket,
                                   'MYENV_S3_BACKEND_BUCKET': 'notThisBucket',
                                   'myenv_S3_BACKEND_BUCKET': 'notThisBucketEither')
            def plugin = new S3BackendPlugin()

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }
    }

    @Nested
    public class GetRegion {
        @Test
        void shouldReturnTheValueOfDefaultS3BackendRegion() {
            String expectedRegion = 'defaultRegion'
            MockJenkinsfile.withEnv('DEFAULT_S3_BACKEND_REGION': expectedRegion)
            def plugin = new S3BackendPlugin()

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfS3BackendRegion() {
            String expectedRegion = 'region'
            MockJenkinsfile.withEnv('S3_BACKEND_REGION': expectedRegion)
            def plugin = new S3BackendPlugin()

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfEnvironmentSpecificS3BackendRegion() {
            String expectedRegion = 'environmentSpecificRegion'
            MockJenkinsfile.withEnv('MYENV_S3_BACKEND_REGION': expectedRegion)
            def plugin = new S3BackendPlugin()

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfEnvironmentSpecificS3BackendRegionCaseInsensitive() {
            String expectedRegion = 'environmentSpecificRegion'
            MockJenkinsfile.withEnv('myenv_S3_BACKEND_REGION': expectedRegion)
            def plugin = new S3BackendPlugin()

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldPreferS3BackendRegionOverEnvironmentSpecificRegion() {
            String expectedRegion = 'thisRegion'
            MockJenkinsfile.withEnv(
                'S3_BACKEND_REGION': expectedRegion,
                'myenv_S3_BACKEND_REGION': 'notThisRegion'
            )
            def plugin = new S3BackendPlugin()

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldPreferEnvironmentSpecificRegionOverDeprecatedDefaultS3BackendRegion() {
            String expectedRegion = 'thisRegion'
            MockJenkinsfile.withEnv(
                'MYENV_S3_BACKEND_REGION': expectedRegion,
                'DEFAULT_S3_BACKEND_REGION': "notThisRegion"
            )
            def plugin = new S3BackendPlugin()

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }
    }

    @Nested
    public class GetKey {
        @Test
        void shouldBeGeneratedFromRepoSlugAndEnvironment() {
            def plugin = new S3BackendPlugin()
            MockJenkinsfile.withStandardizedRepoSlug('Org/App')

            String actualKey = plugin.getKey('myenv')

            assertThat(actualKey, is("terraform/Org/App/myenv"))
        }

        @Test
        void shouldBeGeneratedFromTheCustomKeyPattern() {
            def plugin = new S3BackendPlugin()
            plugin.keyPattern = { environment -> "newPatternFor${environment}" }

            String actualKey = plugin.getKey('myenv')

            assertThat(actualKey, is("newPatternFormyenv"))
        }
    }

    @Nested
    public class GetDynamoTable {
        @Test
        void shouldReturnDeprecatedS3BackendDynamoTableLockValue() {
            String expectedTable = 'myDeprecatedDynamoTable'
            MockJenkinsfile.withEnv(MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK: expectedTable)
            def plugin = new S3BackendPlugin()

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnS3BackendDynamodbTableValue() {
            String expectedTable = 'myDynamoTable'
            MockJenkinsfile.withEnv(S3_BACKEND_DYNAMODB_TABLE: expectedTable)
            def plugin = new S3BackendPlugin()

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendDynamodbTableValue() {
            String expectedTable = 'myEnvDynamoTable'
            MockJenkinsfile.withEnv(MYENV_S3_BACKEND_DYNAMODB_TABLE: expectedTable)
            def plugin = new S3BackendPlugin()

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendDynamodbTableValueCaseInsensitive() {
            String expectedTable = 'myEnvDynamoTable'
            MockJenkinsfile.withEnv(myenv_S3_BACKEND_DYNAMODB_TABLE: expectedTable)
            def plugin = new S3BackendPlugin()

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldPreferS3BackendDynamodbTableOverEnvironmentSpecificValue() {
            String expectedTable = 'thisTable'
            MockJenkinsfile.withEnv(
                S3_BACKEND_DYNAMODB_TABLE: expectedTable,
                MYENV_S3_BACKEND_DYNAMODB_TABLE: 'notThisTable'
            )
            def plugin = new S3BackendPlugin()

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldPreferS3BackendDynamodbTableOverDeprecatedValue() {
            String expectedTable = 'thisTable'
            MockJenkinsfile.withEnv(
                S3_BACKEND_DYNAMODB_TABLE: expectedTable,
                MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK: 'notThisTable'
            )
            def plugin = new S3BackendPlugin()

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }
    }

    @Nested
    public class GetEncrypt {
        @Test
        void shouldReturnS3BackendEncryptValue() {
            String expectedValue = 'true'
            MockJenkinsfile.withEnv(S3_BACKEND_ENCRYPT: expectedValue)
            def plugin = new S3BackendPlugin()

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendEncryptValue() {
            String expectedValue = 'true'
            MockJenkinsfile.withEnv(MYENV_S3_BACKEND_ENCRYPT: expectedValue)
            def plugin = new S3BackendPlugin()

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendEncryptValueCaseInsensitive() {
            String expectedValue = 'true'
            MockJenkinsfile.withEnv(myenv_S3_BACKEND_ENCRYPT: expectedValue)
            def plugin = new S3BackendPlugin()

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }

        @Test
        void shouldPreferS3BackendEncryptOverEnvironmentSpecificValue() {
            String expectedValue = 'true'
            MockJenkinsfile.withEnv(
                S3_BACKEND_ENCRYPT: expectedValue,
                MYENV_S3_BACKEND_ENCRYPT: 'false'
            )
            def plugin = new S3BackendPlugin()

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }
    }

    @Nested
    public class GetKmsKeyId {
        @Test
        void shouldReturnS3BackendKmsKeyIdValue() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            MockJenkinsfile.withEnv('S3_BACKEND_KMS_KEY_ID': expectedArn)
            def plugin = new S3BackendPlugin()

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendKmsKeyIdValue() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            MockJenkinsfile.withEnv(MYENV_S3_BACKEND_KMS_KEY_ID: expectedArn)
            def plugin = new S3BackendPlugin()

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendKmsKeyIdValueCaseInsensitive() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            MockJenkinsfile.withEnv(myenv_S3_BACKEND_KMS_KEY_ID: expectedArn)
            def plugin = new S3BackendPlugin()

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }

        @Test
        void shouldPreferS3BackendKmsKeyIdOverEnvironmentSpecificValue() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            MockJenkinsfile.withEnv(
                S3_BACKEND_KMS_KEY_ID: expectedArn,
                MYENV_S3_BACKEND_KMS_KEY_ID: 'arn:aws:kms:us-east-1:000000000000:key/e665286a-12bc-471f-97fa-38c3eb412074'
            )
            def plugin = new S3BackendPlugin()

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }
    }
}
