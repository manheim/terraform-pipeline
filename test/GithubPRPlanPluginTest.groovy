import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.not
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static TerraformEnvironmentStage.PLAN;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class GithubPRPlanPluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformPlanCommand() {
            GithubPRPlanPlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(GithubPRPlanPlugin.class)))
        }

        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            GithubPRPlanPlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(GithubPRPlanPlugin.class)))
        }
    }

    @Nested
    public class Apply {

        @Test
        void addsTeeArgumentToTerraformPlan() {
            GithubPRPlanPlugin plugin = new GithubPRPlanPlugin()
            TerraformPlanCommand command = new TerraformPlanCommand()

            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-out=tfplan"))
            assertThat(result, containsString("2>plan.err | tee plan.out"))
        }

        @Test
        void decoratesTheTerraformEnvironmentStage()  {
            GithubPRPlanPlugin plugin = new GithubPRPlanPlugin()
            def environment = spy(new TerraformEnvironmentStage())
            MockJenkinsfile.withEnv(
                'BRANCH_NAME': 'master',
                'BUILD_URL': 'https://my-jenkins/job/my-org/job/my-repo/job/PR-1/2/'
            )

            plugin.apply(environment)

            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.PLAN), any(Closure.class))
        }

    }

    @Nested
    class GetRepoSlug {
        @Test
        void returnsTheProvidedRepoSlug() {
            String expectedSlug = 'some/slug'
            GithubPRPlanPlugin.withRepoSlug(expectedSlug)
            def plugin = new GithubPRPlanPlugin()

            String actualSlug = plugin.getRepoSlug()

            assertEquals(expectedSlug, actualSlug)
        }

        @Test
        void defaultsToCurrentRepoSlug() {
            def expectedOrg = 'someOrg'
            def expectedRepo = 'someRepo'
            MockJenkinsfile.withParsedScmUrl([organization: expectedOrg, repo: expectedRepo])
            def plugin = new GithubPRPlanPlugin()

            String actualSlug = plugin.getRepoSlug()

            assertEquals("${expectedOrg}/${expectedRepo}".toString(), actualSlug.toString())
        }

    }

    @Nested
    class GetRepoHost {
        @Test
        void returnsTheProvidedHost() {
            String expectedHost = 'somehost'
            GithubPRPlanPlugin.withRepoHost(expectedHost)
            def plugin = new GithubPRPlanPlugin()

            String actualHost = plugin.getRepoHost()

            assertEquals(expectedHost, actualHost)
        }

        @Test
        void defaultsToTheHostOfTheProject() {
            MockJenkinsfile.withParsedScmUrl([protocol: 'https', domain: 'my.github.com'])
            def plugin = new GithubPRPlanPlugin()

            String actualHost = plugin.getRepoHost()

            assertEquals('https://my.github.com', actualHost)
        }

        @Test
        void defaultsToTheProtocolOfTheProject() {
            MockJenkinsfile.withParsedScmUrl([protocol: 'http', domain: 'my.github.com'])
            def plugin = new GithubPRPlanPlugin()

            String actualHost = plugin.getRepoHost()

            assertEquals('http://my.github.com', actualHost)
        }

        @Test
        void changesGitProtocolToHttps() {
            MockJenkinsfile.withParsedScmUrl([protocol: 'git', domain: 'my.github.com'])
            def plugin = new GithubPRPlanPlugin()

            String actualHost = plugin.getRepoHost()

            assertEquals('https://my.github.com', actualHost)
        }
    }

    @Nested
    class IsPullRequest {
        @Test
        void returnsTrueWhenBranchNameStartsWithPR() {
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn('PR-thisIsAPullRequest').when(plugin).getBranchName()

            assertTrue(plugin.isPullRequest())
        }

        @Test
        void returnsfalseWhenBranchNameDoesNotStartWithPR() {
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn('ThisIsNotA-PR').when(plugin).getBranchName()

            assertFalse(plugin.isPullRequest())
        }

    }

    @Nested
    class GetPullRequestNumber {
        @Test
        void parsesTheNumberFromTheBranchName() {
            def expectedNumber = "123"
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn("PR-${expectedNumber}".toString()).when(plugin).getBranchName()

            def actualNumber = plugin.getPullRequestNumber()

            assertEquals(expectedNumber, actualNumber)
        }
    }

    @Nested
    class GetPullRequestCommentUrl {
        @Test
        void constructsTheUrlFromHostRepoSlugAndPrNumber() {
            def repoHost = 'someHost'
            def repoSlug = 'someSlug'
            def pullRequestNumber = 'somePr'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn(repoHost).when(plugin).getRepoHost()
            doReturn(repoSlug).when(plugin).getRepoSlug()
            doReturn(pullRequestNumber).when(plugin).getPullRequestNumber()

            def commentUrl = plugin.getPullRequestCommentUrl()

            assertEquals("${repoHost}/api/v3/repos/${repoSlug}/issues/${pullRequestNumber}/comments".toString(), commentUrl)
        }
    }

    @Nested
    class GetPlanOutput {
        @Test
        void readsContentsFromPlanOutFile() {
            def planOutFileContent = 'blahblahblah'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn(planOutFileContent).when(plugin).readFile('plan.out')
            doReturn(null).when(plugin).readFile('plan.err')

            def actualOutput = plugin.getPlanOutput()

            assertThat(actualOutput, containsString(planOutFileContent))
        }

        @Test
        void stripsWhitespaceEncodingFromContentsFromPlanOutFile() {
            def expectedOutput = 'blahblah'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn("   ${expectedOutput}   ".toString()).when(plugin).readFile('plan.out')
            doReturn(null).when(plugin).readFile('plan.err')

            def actualOutput = plugin.getPlanOutput()

            assertEquals(expectedOutput, actualOutput)
        }

        @Test
        void stripsAnsiColorEncodingFromContentsFromPlanOutFile() {
            def colorEncoding = '\u001b[32m'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn("${colorEncoding}blablah${colorEncoding}".toString()).when(plugin).readFile('plan.out')
            doReturn(null).when(plugin).readFile('plan.err')

            def planOutput = plugin.getPlanOutput()

            assertThat(planOutput, not(containsString(colorEncoding)))
        }

        @Nested
        class WithErrorOutput {
            @Test
            void includesContentsFromPlanErrorFileIfPresent() {
                def planErrorFileContent = 'errorcontent'
                def plugin = spy(new GithubPRPlanPlugin())
                doReturn('blahblah').when(plugin).readFile('plan.out')
                doReturn(planErrorFileContent).when(plugin).readFile('plan.err')

                def actualOutput = plugin.getPlanOutput()

                assertThat(actualOutput, containsString(planErrorFileContent))
            }

            @Test
            void stripsWhitespaceFromContentsFromPlanErrorFile() {
                def planOutput = 'planOut'
                def planError = 'planError'
                def plugin = spy(new GithubPRPlanPlugin())
                doReturn(planOutput).when(plugin).readFile('plan.out')
                doReturn("   ${planError}   ".toString()).when(plugin).readFile('plan.err')

                def actualOutput = plugin.getPlanOutput()

                assertEquals("${planOutput}\nSTDERR:\n${planError}".toString(), actualOutput)
            }

            @Test
            void stripsAnsiColorEncodingFromContentsFromPlanErrorFile() {
                def colorEncoding = '\u001b[32m'
                def plugin = spy(new GithubPRPlanPlugin())
                doReturn('planOut').when(plugin).readFile('plan.out')
                doReturn("${colorEncoding}blablah${colorEncoding}".toString()).when(plugin).readFile('plan.err')

                def actualOutput = plugin.getPlanOutput()

                assertThat(actualOutput, not(containsString(colorEncoding)))
            }

            @Test
            void ignoresPlanErrorFileContentIfEmpty() {
                def planOutput = 'planOut'
                def planError = ''
                def plugin = spy(new GithubPRPlanPlugin())
                doReturn(planOutput).when(plugin).readFile('plan.out')
                doReturn(planError).when(plugin).readFile('plan.err')

                def actualOutput = plugin.getPlanOutput()

                assertEquals(planOutput, actualOutput)
            }
        }
    }

    @Nested
    class GetCommentBody {
        @Test
        void usesThePlanOutput()  {
            def planOutput = 'someOutput'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn(planOutput).when(plugin).getPlanOutput()
            doReturn('someResult').when(plugin).getBuildResult()
            doReturn('someUrl').when(plugin).getBuildUrl()

            def actualComment = plugin.getCommentBody('myenv')

            assertThat(actualComment, containsString(planOutput))
        }

        @Test
        void usesTheCurrentBuildResult()  {
            def expectedBuildResult = 'expectedResult'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn('someOutput').when(plugin).getPlanOutput()
            doReturn(expectedBuildResult).when(plugin).getBuildResult()
            doReturn('someUrl').when(plugin).getBuildUrl()

            def actualComment = plugin.getCommentBody('myenv')

            assertThat(actualComment, containsString(expectedBuildResult))
        }

        @Test
        void usesTheCurrentBuildUrl()  {
            def expectedBuildUrl = 'expectedBuildUrl'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn('someOutput').when(plugin).getPlanOutput()
            doReturn('someResult').when(plugin).getBuildResult()
            doReturn(expectedBuildUrl).when(plugin).getBuildUrl()

            def actualComment = plugin.getCommentBody('myenv')

            assertThat(actualComment, containsString(expectedBuildUrl))
        }

        @Test
        void usesTheGivenEnvironment()  {
            def expectedEnvironment = 'expectedEnv'
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn('someOutput').when(plugin).getPlanOutput()
            doReturn('someResult').when(plugin).getBuildResult()
            doReturn('someUrl').when(plugin).getBuildUrl()

            def actualComment = plugin.getCommentBody(expectedEnvironment)

            assertThat(actualComment, containsString(expectedEnvironment))
        }
    }

    @Nested
    class PostPullRequestComment {
        // This needs to be tested better than 'do not blow up'
        @Test
        void doesNotBlowUp() {
            MockJenkinsfile.withParsedScmUrl([protocol: 'git', domain: 'my.github.com'])
            Jenkinsfile.original = spy(new DummyJenkinsfile())
            def plugin = spy(new GithubPRPlanPlugin())
            doReturn('HTTP/1.1 201 Created').when(plugin).readFile('comment.headers')
            doReturn('{ "id": "someId", "html_url": "some_url" }').when(Jenkinsfile.original).sh(anyObject())

            plugin.postPullRequestComment('someUrl', 'myPrComment')
        }

        // Test executes passed closure
        // Test raises error on non HTTP/1.1 201 Created
        // Uses the correct pullRequestUrl
        // Uses the correct git auth token
        // Chunks requests correct if over 65536
    }
}
