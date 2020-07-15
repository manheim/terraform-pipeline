class DummyJenkinsfile {
    public docker

    public DummyJenkinsfile() {
        docker = this
    }

    public docker(Closure closure = null) {
        return this
    }

    public image(String dockerImage) { return this }
    public inside(String dockerImage, Closure closure) {
        closure()
        return this
    }
    public build(String image, String options) { return this }
    public echo(String message) { println "DummyJenkinsfile.echo ${message}" }
    public pwd(Map options) { return '/DummyJenkinsfile/currentDir' }
    public writeFile(Map options) { println "DummyJenkinsfile.writeFile: ${options.toString()}" }
    public readFile(String filename) { return "Dummyjenkinsfile.readFile(${filename}): some content" }
    public fileExists(String filename) { return false }
    public sh(Map options) { println "DummyJenkinsfile.sh: ${options.toString()}"; return 'DummyJenkinsfile.sh output' }
}
