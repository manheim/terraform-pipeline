class TerraformFormatCommand {
    private static boolean check = false
    private static boolean recursive = false
    private static boolean diff = false

    public String toString() {
        def parts = []
        parts << 'terraform fmt'

        if (check) {
            parts << '-check=true'
        }

        if (recursive) {
            println "recursive is default in Terraform 0.11.x - this is an unsupported option"
        }

        if (diff) {
            parts << '-diff=true'
        }

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
    }
}
