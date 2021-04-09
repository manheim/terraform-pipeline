class FlywayCommand implements Resettable {
    private String command
    private String binary = "flyway"
    private static String locations
    private static String url

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

        return pieces.join(' ')
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
    }
}
