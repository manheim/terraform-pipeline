import static TerraformEnvironmentStage.CONFIRM
import static TerraformEnvironmentStage.APPLY

public class ConditionalApplyPlugin implements TerraformEnvironmentStagePlugin, Resettable {

    private static DEFAULT_BRANCHES = ['master']
    private static branches = DEFAULT_BRANCHES

    public static void withApplyOnBranch(String... enabledBranches) {
        branches = enabledBranches.clone()
    }

    public static void reset() {
        branches = DEFAULT_BRANCHES
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorateAround(CONFIRM, onlyOnExpectedBranch())
        stage.decorateAround(APPLY, onlyOnExpectedBranch())
    }

    public Closure onlyOnExpectedBranch() {
        return  { closure ->
            if (shouldApply()) {
                closure()
            } else {
                echo "This stage can only be run on the '${branches}' branches, but this pipeline is currently running on branch '${Jenkinsfile.instance.getEnv().BRANCH_NAME}'.  Skipping stage."
            }
        }
    }

    public boolean shouldApply() {
        if (branches.contains(Jenkinsfile.instance.getEnv().BRANCH_NAME)) {
            println("Current branch '${Jenkinsfile.instance.getEnv().BRANCH_NAME}' matches expected branches '${branches}', stage branch-condition is met and will run.")
            return true
        } else if (null == Jenkinsfile.instance.getEnv().BRANCH_NAME) {
            println("Current branch is null - you're probably using a single-branch job which doesn't make your branch name available.  Assume that apply should be enabled.")
            return true
        }

        return false
    }
}
