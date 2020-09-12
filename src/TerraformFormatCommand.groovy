class TerraformFormatCommand {
    private static boolean recursive = false
    private static boolean diff = false

    public String toString() {
        def parts = []
        parts = ['terraform fmt -check']

        if (recursive) {
            parts << '-recursive'
        }

        if (diff) {
            parts << '-diff'
        }

        return parts.join(' ')
    }

    public static withRecursive() {
        recursive = true
        return this
    }

    public static withDiff() {
        diff = true
        return this
    }

    public static reset() {
        recursive = false
        diff = false
    }
}
