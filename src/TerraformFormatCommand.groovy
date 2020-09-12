class TerraformFormatCommand {
    private static boolean check = false
    private static Closure checkOptionPattern
    private static boolean recursive = false
    private static Closure recursiveOptionPattern
    private static boolean diff = false
    private static Closure diffOptionPattern

    public String toString() {
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

    public static reset() {
        check = false
        recursive = false
        diff = false
        checkOptionPattern = null
        recursiveOptionPattern = null
        diffOptionPattern = null
    }
}
