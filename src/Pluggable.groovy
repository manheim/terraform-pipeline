/**
 * The `Pluggable` trait can be used to add plugin management to a class. It
 * takes as a type parameter the plugin type it accepts.
 */
trait Pluggable<T> implements Resettable {
    private static plugins = []
    private appliedPlugins = []

    /**
     * Assures that all plugins are applied, and are applied at most once. It
     * can be safely called multiple times.
     */
    public applyPlugins() {
        def remainingPlugins = plugins - appliedPlugins

        for (T plugin in remainingPlugins) {
            plugin.apply(this)
            appliedPlugins << plugin
        }
    }

    /**
     * Accepts a plugin of the appropriate type and adds it to the list of plugins.
     *
     * @param plugin The plugin to add
     */
    public static void addPlugin(T plugin) {
        plugins << plugin
    }

    public static void setPlugins(plugins) {
        this.plugins = plugins
    }

    public static getPlugins() {
        return plugins
    }

    /**
     * Reset plugins will reset the plugin list to a default set of plugins.
     *
     * @param defaultPlugins list of plugins to set, default: []
     */
    public static void resetPlugins(defaultPlugins = []) {
        this.plugins = defaultPlugins.clone()
    }

    public static void reset() {
        this.resetPlugins()
    }
}
