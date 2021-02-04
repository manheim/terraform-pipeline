import static TerraformEnvironmentStage.ALL
import static TerraformEnvironmentStage.INIT_COMMAND
import static TerraformEnvironmentStage.PLAN
import static TerraformEnvironmentStage.PLAN_COMMAND
import static TerraformEnvironmentStage.APPLY
import static TerraformEnvironmentStage.APPLY_COMMAND

class TerraformEnvironmentStageShellHookPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    static hooks = [
        (ALL): new HookPoint(ALL),
        (INIT_COMMAND): new HookPoint(INIT_COMMAND),
        (PLAN): new HookPoint(PLAN),
        (PLAN_COMMAND): new HookPoint(PLAN_COMMAND),
        (APPLY): new HookPoint(APPLY),
        (APPLY_COMMAND): new HookPoint(APPLY_COMMAND)
    ]

    public static void init() {
        TerraformEnvironmentStageShellHookPlugin plugin = new TerraformEnvironmentStageShellHookPlugin()
        TerraformEnvironmentStage.addPlugin(plugin)
    }

    public static withHook(String hookPoint, String shellCommand, WhenToRun whenToRun = WhenToRun.ON_SUCCESS) {
        switch ( whenToRun ) {
            case WhenToRun.BEFORE:
                hooks[hookPoint].runBefore = shellCommand
                break
            case WhenToRun.AFTER:
                hooks[hookPoint].runAfterAlways = shellCommand
                break
            case WhenToRun.ON_FAILURE:
                hooks[hookPoint].runAfterOnFailure = shellCommand
                break
            case WhenToRun.ON_SUCCESS:
                hooks[hookPoint].runAfterOnSuccess = shellCommand
                break
        }
        return this
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        hooks.each { pointName, hookPoint ->
            if (hookPoint.isConfigured()) {
                stage.decorate(pointName, hookPoint.getClosure())
            }
        }
    }

    public static void reset() {
        hooks = [
            (ALL): new HookPoint(ALL),
            (INIT_COMMAND): new HookPoint(INIT_COMMAND),
            (PLAN): new HookPoint(PLAN),
            (PLAN_COMMAND): new HookPoint(PLAN_COMMAND),
            (APPLY): new HookPoint(APPLY),
            (APPLY_COMMAND): new HookPoint(APPLY_COMMAND)
        ]
    }
}
