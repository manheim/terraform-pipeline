import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.*

@RunWith(HierarchicalContextRunner.class)
class S3BackendPluginTest {
    @After
    void resetJenkins() {
        when(Jenkinsfile.instance.getEnv()).thenReturn([:])
    }

    private configureJenkins(Map config = [:]) {
        Jenkinsfile.instance = mock(Jenkinsfile.class)
        when(Jenkinsfile.instance.getStandardizedRepoSlug()).thenReturn(config.repoSlug)
        when(Jenkinsfile.instance.getEnv()).thenReturn(config.env ?: [:])
    }

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
            configureJenkins(repoSlug: repoSlug)

            String environment = "myEnv"
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=key=terraform/${repoSlug}/${environment}"))
        }

        @Test
        void addsBucketPreDefinedByEnvironmentAsBackendParameter() {
            String expectedBucket = 'bucket'
            configureJenkins(env: [MYENV_S3_BACKEND_BUCKET: expectedBucket])

            String environment = "myEnv"
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=bucket=${expectedBucket}"))
        }

        @Test
        void addsBucketRegionUsingPreDefinedEnvironmentVariableAsBackendParameter() {
            String expectedRegion = 'theFarEast'
            configureJenkins(env: [ 'DEFAULT_S3_BACKEND_REGION': expectedRegion ])

            String environment = "myEnv"
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=region=${expectedRegion}"))
        }

        @Test
        void addsDynamoDbTableAsBackendParameter() {
            String dynamodb_table = 'terraform-state-lock-dynamo'
            configureJenkins(env: [ 'MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK': dynamodb_table ])

            String environment = "myEnv"
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-backend-config=dynamodb_table=${dynamodb_table}"))
        }

        @Test
        void skipsDynamoDbTableAsBackendParameter() {
            configureJenkins()

            String environment = "myEnv"
            S3BackendPlugin plugin = new S3BackendPlugin()
            TerraformInitCommand command = new TerraformInitCommand(environment)

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, not(containsString("-backend-config=dynamodb_table=")))
        }

        @Test
        void isAddedAndUsesCustomizedPatternFolderKeyAsBackendParameter() {
            String repoSlug = 'myOrg/myRepo'
            configureJenkins(repoSlug: repoSlug)

            S3BackendPlugin.keyPattern = { String env -> "customPatternFor/${repoSlug}/entropy/${env}" }
            S3BackendPlugin plugin = new S3BackendPlugin()
            
            String environment = "myEnv"
            TerraformInitCommand command = new TerraformInitCommand(environment)
            plugin.apply(command)
            assertThat(command.toString(), containsString("-backend-config=key=customPatternFor/${repoSlug}/entropy/${environment}"))
        }
    }

    public class GetBackend {
        @Test
        void shouldReturnTheValueOfS3BackendBucket() {
            def plugin = new S3BackendPlugin()
            String expectedBucket = 'defaultBucket'

            configureJenkins(env: ['S3_BACKEND_BUCKET': expectedBucket])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldReturnTheValueOfTheEnvironmentSpecificS3BackendBucket() {
            def plugin = new S3BackendPlugin()
            String expectedBucket = 'myBucket'

            configureJenkins(env: ['MYENV_S3_BACKEND_BUCKET': expectedBucket])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldReturnTheValueOfTheEnvironmentSpecificS3BackendBucketCaseInsensitive() {
            def plugin = new S3BackendPlugin()
            String expectedBucket = 'myBucket'

            configureJenkins(env: ['myenv_S3_BACKEND_BUCKET': expectedBucket])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }

        @Test
        void shouldPreferS3BackendBucketOverEnvironmentSpecificBucket() {
            def plugin = new S3BackendPlugin()
            String expectedBucket = "thisBucket"
            configureJenkins(env: ['S3_BACKEND_BUCKET': expectedBucket,
                                   'MYENV_S3_BACKEND_BUCKET': 'notThisBucket',
                                   'myenv_S3_BACKEND_BUCKET': 'notThisBucketEither'])

            String actualBucket = plugin.getBucket('myenv')

            assertThat(actualBucket, is(expectedBucket))
        }
    }

    public class GetRegion {
        @Test
        void shouldReturnTheValueOfDefaultS3BackendRegion() {
            def plugin = new S3BackendPlugin()
            String expectedRegion = 'defaultRegion'

            configureJenkins(env: ['DEFAULT_S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfS3BackendRegion() {
            def plugin = new S3BackendPlugin()
            String expectedRegion = 'region'

            configureJenkins(env: ['S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfEnvironmentSpecificS3BackendRegion() {
            def plugin = new S3BackendPlugin()
            String expectedRegion = 'environmentSpecificRegion'

            configureJenkins(env: ['MYENV_S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldReturnTheValueOfEnvironmentSpecificS3BackendRegionCaseInsensitive() {
            def plugin = new S3BackendPlugin()
            String expectedRegion = 'environmentSpecificRegion'

            configureJenkins(env: ['myenv_S3_BACKEND_REGION': expectedRegion])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldPreferS3BackendRegionOverEnvironmentSpecificRegion() {
            def plugin = new S3BackendPlugin()
            String expectedRegion = 'thisRegion'

            configureJenkins(env: [
                'S3_BACKEND_REGION': expectedRegion,
                'myenv_S3_BACKEND_REGION': 'notThisRegion'
            ])

            String actualRegion = plugin.getRegion('myenv')

            assertThat(actualRegion, is(expectedRegion))
        }

        @Test
        void shouldPreferEnvironmentSpecificRegionOverDeprecatedDefaultS3BackendRegion() {
            def plugin = new S3BackendPlugin()
            String expectedRegion = 'thisRegion'

            configureJenkins(env: [
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
            def plugin = new S3BackendPlugin()

            configureJenkins(repoSlug: 'Org/App')

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
            def plugin = new S3BackendPlugin()
            String expectedTable = 'myDeprecatedDynamoTable'

            configureJenkins(env: [MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnS3BackendDynamodbTableValue() {
            def plugin = new S3BackendPlugin()
            String expectedTable = 'myDynamoTable'

            configureJenkins(env: [S3_BACKEND_DYNAMODB_TABLE: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendDynamodbTableValue() {
            def plugin = new S3BackendPlugin()
            String expectedTable = 'myEnvDynamoTable'

            configureJenkins(env: [MYENV_S3_BACKEND_DYNAMODB_TABLE: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldReturnEnvironmentSpecificS3BackendDynamodbTableValueCaseInsensitive() {
            def plugin = new S3BackendPlugin()
            String expectedTable = 'myEnvDynamoTable'

            configureJenkins(env: [myenv_S3_BACKEND_DYNAMODB_TABLE: expectedTable])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldPreferS3BackendDynamodbTableOverEnvironmentSpecificValue() {
            def plugin = new S3BackendPlugin()
            String expectedTable = 'thisTable'

            configureJenkins(env: [
                S3_BACKEND_DYNAMODB_TABLE: expectedTable,
                MYENV_S3_BACKEND_DYNAMODB_TABLE: 'notThisTable'
            ])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }

        @Test
        void shouldPreferS3BackendDynamodbTableOverDeprecatedValue() {
            def plugin = new S3BackendPlugin()
            String expectedTable = 'thisTable'

            configureJenkins(env: [
                S3_BACKEND_DYNAMODB_TABLE: expectedTable,
                MYENV_S3_BACKEND_DYNAMO_TABLE_LOCK: 'notThisTable'
            ])

            String actualTable = plugin.getDynamodbTable('myenv')

            assertThat(actualTable, is(expectedTable))
        }
    }
}
