trait Pluggable<T> {
    private static plugins = []
    private appliedPlugins = []

    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (T plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    public static void addPlugin(T plugin) {
        plugins << plugin
    }

    public static void setPlugins(plugins) {
        this.plugins = plugins
    }

    public static getPlugins() {
        return plugins
    }

    public String toString() {
        applyPlugins()
        // Chain the toString call to the next trait in the chain
        return super.toString()
    }
}
