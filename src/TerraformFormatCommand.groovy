class TerraformFormatCommand {
    private static boolean recursive = false

    public String toString() {
        def parts = []
        parts = ['terraform fmt -check']

        if (recursive) {
            parts << '-recursive'
        }

        return parts.join(' ')
    }

    public static withRecursive() {
        recursive = true
        return this
    }

    public static reset() {
        recursive = false
    }
}
