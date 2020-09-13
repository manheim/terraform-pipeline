class TerraformFormatCommand {
    private static globalPlugins = []
    private appliedPlugins = []
    private static boolean check = false
    private static boolean recursive = false
    private static boolean diff = false

    private Closure checkOptionPattern
    private Closure recursiveOptionPattern
    private Closure diffOptionPattern

    public String toString() {
        applyPluginsOnce()

        def pattern
        def parts = []
        parts << 'terraform fmt'

        pattern = checkOptionPattern ?: { it ? '-check=true' : null }
        parts << pattern(check)

        pattern = recursiveOptionPattern ?: { println "recursive is default in Terraform 0.11.x  - this is an unsupported option" }
        parts << pattern(recursive)

        pattern = diffOptionPattern ?: { it ? '-diff=true' : null }
        parts << pattern(diff)

        parts.removeAll { it == null }
        return parts.join(' ')
    }

    public static addPlugin(TerraformFormatCommandPlugin plugin) {
        this.globalPlugins << plugin
    }

    private applyPluginsOnce() {
        def remainingPlugins = globalPlugins - appliedPlugins

        for (TerraformValidateCommandPlugin plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static withCheck(newValue = true) {
        check = newValue
        return this
    }

    public static boolean isCheckEnabled() {
        return check
    }

    public static withRecursive(newValue = true) {
        recursive = newValue
        return this
    }

    public static withDiff(newValue = true) {
        diff = newValue
        return this
    }

    public withCheckOptionPattern(Closure pattern) {
        checkOptionPattern = pattern
        return this
    }

    public withRecursiveOptionPattern(Closure pattern) {
        recursiveOptionPattern = pattern
        return this
    }

    public withDiffOptionPattern(Closure pattern) {
        diffOptionPattern = pattern
        return this
    }

    public static reset() {
        check = false
        recursive = false
        diff = false
        globalPlugins = []
    }
}
