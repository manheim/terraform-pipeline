import static TerraformEnvironmentStage.CONFIRM
import static TerraformEnvironmentStage.APPLY

public class ConditionalApplyPlugin implements TerraformEnvironmentStagePlugin, Resettable {

    private static enabled = true
    private static DEFAULT_BRANCHES = ['main', 'master']
    private static branches = DEFAULT_BRANCHES
    private static environments = []

    public static withApplyOnBranch(String... enabledBranches) {
        branches = enabledBranches.clone()
        return this
    }

    public static withApplyOnEnvironment(String... enabledEnvironments) {
        environments = enabledEnvironments.clone()
        return this
    }

    public static disable() {
        enabled = false
    }

    public static void reset() {
        branches = DEFAULT_BRANCHES
        enabled = true
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        stage.decorateAround(CONFIRM, onlyOnExpectedBranch(stage.getEnvironment()))
        stage.decorateAround(APPLY, onlyOnExpectedBranch(stage.getEnvironment()))
    }

    public Closure onlyOnExpectedBranch(String environment) {
        return  { closure ->
            if (shouldApply(environment)) {
                closure()
            } else {
                echo "Skipping Confirm/Apply steps, based on the configuration of ConditionalApplyPlugin."
            }
        }
    }

    public boolean shouldApply(String environment) {
        if (!enabled) {
            return true
        }

        if (environments.contains(environment)) {
            return true
        }

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
