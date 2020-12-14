import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.INIT_COMMAND
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.PLAN_COMMAND
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.APPLY_COMMAND

class HookPoint {
    String runBefore = null
    String runAfterOnSuccess = null
    String runAfterOnFailure = null
    String runAfterAlways = null
    private String hookName

    HookPoint(String hookName) {
        this.hookName = hookName
    }

    public Boolean isConfigured() {
        return ! (runBefore == null && runAfterOnSuccess == null && runAfterOnFailure == null && runAfterAlways == null)
    }

    public Closure getClosure() {
        return { closure ->
            try {
                if (runBefore != null) { sh runBefore }
                closure()
                if (runAfterOnSuccess != null) { sh runAfterOnSuccess }
            } catch(Exception e) {
                if (runAfterOnFailure != null) { sh runAfterOnFailure }
                throw e
            } finally {
                if (runAfterAlways != null) { sh runAfterAlways }
            }
        }
    }
}

class TerraformEnvironmentStageShellHookPlugin implements TerraformEnvironmentStagePlugin {
    private static hooks = [
        ALL: HookPoint(ALL),
        INIT_COMMAND: HookPoint(INIT_COMMAND),
        PLAN: HookPoint(PLAN),
        PLAN_COMMAND: HookPoint(PLAN_COMMAND),
        APPLY: HookPoint(APPLY),
        APPLY_COMMAND: HookPoint(APPLY_COMMAND)
    ]

    public static final enum WhenToRun {BEFORE, ON_SUCCESS, ON_FAILURE, AFTER}

    public static void init() {
        TerraformEnvironmentStageShellHookPlugin plugin = new TerraformEnvironmentStageShellHookPlugin()
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public static withHook(String hookPoint, String shellCommand, int whenToRun = WhenToRun.ON_SUCCESS) {
        switch ( whenToRun ) {
            case WhenToRun.BEFORE:
                hooks[hookPoint].runBefore = shellCommand
            case WhenToRun.AFTER:
                hooks[hookPoint].runAfterAlways = shellCommand
            case WhenToRun.ON_FAILURE:
                hooks[hookPoint].runAfterOnFailure = shellCommand
            default:
                hooks[hookPoint].runAfterOnSuccess = shellCommand
        }
        return this
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        map.each { pointName, hookPoint ->
            if(hookPoint.isConfigured()) {
                stage.decorate(pointName, hookPoint.getClosure())
            }
        }
    }

    public static void reset() {
        hooks = [
            ALL: HookPoint(ALL),
            INIT_COMMAND: HookPoint(INIT_COMMAND),
            PLAN: HookPoint(PLAN),
            PLAN_COMMAND: HookPoint(PLAN_COMMAND),
            APPLY: HookPoint(APPLY),
            APPLY_COMMAND: HookPoint(APPLY_COMMAND)
        ]
    }
}
