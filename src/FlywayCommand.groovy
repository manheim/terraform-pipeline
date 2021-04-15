class FlywayCommand implements Resettable {
    private String command
    private String binary = "flyway"
    private static String locations
    private static String url
    private static String user

    public FlywayCommand(String command) {
        this.command = command
    }

    public String toString() {
        def pieces = []
        pieces << binary
        pieces << command

        if (locations) {
            pieces << "-locations=${locations}"
        }

        if (url) {
            pieces << "-url=${url}"
        }

        if (user) {
            pieces << "-user=${user}"
        }

        return pieces.join(' ')
    }

    public static withUser(String user) {
        this.user = user
        return this
    }

    public static withLocations(String locations) {
        this.locations = locations
        return this
    }

    public static withUrl(String url) {
        this.url = url
        return this
    }

    public static reset() {
        this.locations = null
        this.url = null
        this.user = null
    }
}
