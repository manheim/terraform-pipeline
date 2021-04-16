class FlywayCommand implements Resettable {
    private String command
    private String binary = "flyway"
    private static String locations
    private static String url
    private static String user
    private static String password
    private static Collection additionalParameters = []

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

        if (password) {
            pieces << "-password=${password}"
        }

        pieces.addAll(additionalParameters)

        return pieces.join(' ')
    }

    public static withUser(String user) {
        this.user = user
        return this
    }

    public static withPassword(String password) {
        this.password = password
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

    public static withAdditionalParameter(String parameter) {
        this.additionalParameters << parameter
        println "withAdditionalParameter: ${parameter}, ${this.additionalParameters}"
        return this
    }

    public static reset() {
        this.locations = null
        this.url = null
        this.user = null
        this.password = null
        this.additionalParameters = []
    }
}
