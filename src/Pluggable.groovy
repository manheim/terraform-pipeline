trait Pluggable<T> implements Resettable {
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

    public static void reset() {
        this.plugins = []
    }
}
