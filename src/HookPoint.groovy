class HookPoint {
    String runBefore = null
    String runAfterOnSuccess = null
    String runAfterOnFailure = null
    String runAfterAlways = null
    private String hookName

    HookPoint(String hookName) {
        this.hookName = hookName
    }

    public String getName() {
        return this.hookName
    }

    public Boolean isConfigured() {
        return ! (this.runBefore == null && this.runAfterOnSuccess == null && this.runAfterOnFailure == null && this.runAfterAlways == null)
    }

    public Closure getClosure() {
        return { closure ->
            try {
                if (this.runBefore != null) { sh this.runBefore }
                closure()
                if (this.runAfterOnSuccess != null) { sh this.runAfterOnSuccess }
            } catch (Exception e) {
                if (this.runAfterOnFailure != null) { sh this.runAfterOnFailure }
                throw e
            } finally {
                if (this.runAfterAlways != null) { sh this.runAfterAlways }
            }
        }
    }
}
