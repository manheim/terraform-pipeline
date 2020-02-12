import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner

@RunWith(HierarchicalContextRunner.class)
class S3BackendPluginTest {
    public class Init {
        @After
        void resetPlugins() {
            TerraformInitCommand.resetPlugins()
        }

        @Test
        void addsS3BackendPluginToListOfPlugins() {
            S3BackendPlugin.init()

            Collection actualPlugins = TerraformInitCommand.getPlugins()
            assertThat(actualPlugins, contains(instanceOf(S3BackendPlugin.class)))
        }
    }

    public class Apply {
        @After
        void reset() {
            S3BackendPlugin.keyPattern = null
        }

        @Test
        void addsEnvironmentSpecificKeyAsBackendParameter() {
            String repoSlug = 'myOrg/myRepo'
            String environment = "myEnv"
            S3BackendPlugin plugin = spy(new S3BackendPlugin())
            when(plugin.getStandardizedRepoSlug()).thenReturn(repoSlug)
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=key=terraform/${repoSlug}/${environment}"))
        }

        @Test
        void addsBucketPreDefinedByEnvironmentAsBackendParameter() {
            String expectedBucket = 'bucket'
            String environment = "myEnv"
            S3BackendPlugin plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([MYENV_S3_BACKEND_BUCKET: expectedBucket])
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=bucket=${expectedBucket}"))
        }

        @Test
        void addsBucketRegionUsingPreDefinedEnvironmentVariableAsBackendParameter() {
            String expectedRegion = 'theFarEast'

            String environment = "myEnv"
            S3BackendPlugin plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([ 'DEFAULT_S3_BACKEND_REGION': expectedRegion])
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=region=${expectedRegion}"))
        }

        @Test
        void addsDynamoDbTableAsBackendParameter() {
            String dynamodb_table = 'terraform-state-lock-dynamo'

            String environment = "myEnv"
            S3BackendPlugin plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([ 'MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK': dynamodb_table ])
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=dynamodb_table=${dynamodb_table}"))
        }

        @Test
        void skipsDynamoDbTableAsBackendParameterWhenNoneSpecified() {
            String environment = "myEnv"
            S3BackendPlugin plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([:])
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-backend-config=dynamodb_table=")))
        }

        @Test
        void isAddedAndUsesCustomizedPatternFolderKeyAsBackendParameter() {
            String repoSlug = 'myOrg/myRepo'
            S3BackendPlugin.keyPattern = { String env -> "customPatternFor/${repoSlug}/entropy/${env}" }
            S3BackendPlugin plugin = spy(new S3BackendPlugin())
            when(plugin.getStandardizedRepoSlug()).thenReturn(repoSlug)

            String environment = "myEnv"
            TerraformInitCommand command = new TerraformInitCommand(environment)
            plugin.apply(command)
            assertThat(command.toString(), containsString("-backend-config=key=customPatternFor/${repoSlug}/entropy/${environment}"))
        }
    }

    public class GetBackend {
        @Test
        void shouldReturnTheValueOfS3BackendBucket() {
            String expectedBucket = 'defaultBucket'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['S3_BACKEND_BUCKET': expectedBucket])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldReturnTheValueOfTheEnvironmentSpecificS3BackendBucket() {
            String expectedBucket = 'myBucket'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['MYENV_S3_BACKEND_BUCKET': expectedBucket])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldReturnTheValueOfTheEnvironmentSpecificS3BackendBucketCaseInsensitive() {
            String expectedBucket = 'myBucket'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['myenv_S3_BACKEND_BUCKET': expectedBucket])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldPreferS3BackendBucketOverEnvironmentSpecificBucket() {
            String expectedBucket = "thisBucket"
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['S3_BACKEND_BUCKET': expectedBucket,
                                   'MYENV_S3_BACKEND_BUCKET': 'notThisBucket',
                                   'myenv_S3_BACKEND_BUCKET': 'notThisBucketEither'])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }
    }

    public class GetRegion {
        @Test
        void shouldReturnTheValueOfDefaultS3BackendRegion() {
            String expectedRegion = 'defaultRegion'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['DEFAULT_S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfS3BackendRegion() {
            String expectedRegion = 'region'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfEnvironmentSpecificS3BackendRegion() {
            String expectedRegion = 'environmentSpecificRegion'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['MYENV_S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfEnvironmentSpecificS3BackendRegionCaseInsensitive() {
            String expectedRegion = 'environmentSpecificRegion'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn(['myenv_S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldPreferS3BackendRegionOverEnvironmentSpecificRegion() {
            String expectedRegion = 'thisRegion'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([
                'S3_BACKEND_REGION': expectedRegion,
                'myenv_S3_BACKEND_REGION': 'notThisRegion'
            ])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldPreferEnvironmentSpecificRegionOverDeprecatedDefaultS3BackendRegion() {
            String expectedRegion = 'thisRegion'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([
                'MYENV_S3_BACKEND_REGION': expectedRegion,
                'DEFAULT_S3_BACKEND_REGION': "notThisRegion"
            ])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }
    }

    public class GetKey {
        @After
        void resetPlugins() {
            S3BackendPlugin.keyPattern = null
        }

        @Test
        void shouldBeGeneratedFromRepoSlugAndEnvironment() {
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getStandardizedRepoSlug()).thenReturn('Org/App')

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

    public class GetDynamoTable {
        @Test
        void shouldReturnDeprecatedS3BackendDynamoTableLockValue() {
            String expectedTable = 'myDeprecatedDynamoTable'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnS3BackendDynamodbTableValue() {
            String expectedTable = 'myDynamoTable'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([S3_BACKEND_DYNAMODB_TABLE: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendDynamodbTableValue() {
            String expectedTable = 'myEnvDynamoTable'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([MYENV_S3_BACKEND_DYNAMODB_TABLE: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendDynamodbTableValueCaseInsensitive() {
            String expectedTable = 'myEnvDynamoTable'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([myenv_S3_BACKEND_DYNAMODB_TABLE: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldPreferS3BackendDynamodbTableOverEnvironmentSpecificValue() {
            String expectedTable = 'thisTable'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([
                S3_BACKEND_DYNAMODB_TABLE: expectedTable,
                MYENV_S3_BACKEND_DYNAMODB_TABLE: 'notThisTable'
            ])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldPreferS3BackendDynamodbTableOverDeprecatedValue() {
            String expectedTable = 'thisTable'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([
                S3_BACKEND_DYNAMODB_TABLE: expectedTable,
                MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK: 'notThisTable'
            ])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }
    }

    public class GetEncrypt {
        @Test
        void shouldReturnS3BackendEncryptValue() {
            String expectedValue = 'true'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([S3_BACKEND_ENCRYPT: expectedValue])

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendEncryptValue() {
            String expectedValue = 'true'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([MYENV_S3_BACKEND_ENCRYPT: expectedValue])

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendEncryptValueCaseInsensitive() {
            String expectedValue = 'true'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([myenv_S3_BACKEND_ENCRYPT: expectedValue])

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }

        @Test
        void shouldPreferS3BackendEncryptOverEnvironmentSpecificValue() {
            String expectedValue = 'true'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([
                S3_BACKEND_ENCRYPT: expectedValue,
                MYENV_S3_BACKEND_ENCRYPT: 'false'
            ])

            String actualValue = plugin.getEncrypt('myenv')

            assertThat(expectedValue, is(actualValue))
        }
    }

    public class GetKmsKeyId {
        @Test
        void shouldReturnS3BackendKmsKeyIdValue() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([S3_BACKEND_KMS_KEY_ID: expectedArn])

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendKmsKeyIdValue() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([MYENV_S3_BACKEND_KMS_KEY_ID: expectedArn])

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendKmsKeyIdValueCaseInsensitive() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([myenv_S3_BACKEND_KMS_KEY_ID: expectedArn])

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }

        @Test
        void shouldPreferS3BackendKmsKeyIdOverEnvironmentSpecificValue() {
            String expectedArn = 'arn:aws:kms:us-east-1:000000000000:key/eed43b74-c0ff-475f-abab-d0e31b85ee8d'
            def plugin = spy(new S3BackendPlugin())
            when(plugin.getEnv()).thenReturn([
                S3_BACKEND_KMS_KEY_ID: expectedArn,
                MYENV_S3_BACKEND_KMS_KEY_ID: 'arn:aws:kms:us-east-1:000000000000:key/e665286a-12bc-471f-97fa-38c3eb412074'
            ])

            String actualArn = plugin.getKmsKeyId('myenv')

            assertThat(actualArn, is(expectedArn))
        }
    }
}
