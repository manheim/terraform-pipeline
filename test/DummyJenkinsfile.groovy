class DummyJenkinsfile {
    public docker
    public scm
    public env = [:]
    public static BRANCH_NAME

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
    public sh(String command) { println "DummyJenkinsfile.sh: ${command.toString()}"; return 'DummyJenkinsfile.sh output' }
    public sh(Map options) { println "DummyJenkinsfile.sh: ${options.toString()}"; return 'DummyJenkinsfile.sh output' }
    public string(Map options) {  println "DummyJenkinsfile.string: ${options.toString()}" }
    public parameters(params) {  println "DummyJenkinsfile.parameters: ${params.toString()}" }
    public properties(props) {  println "DummyJenkinsfile.properties: ${props.toString()}" }
    public node(String nodeName, Closure closure) {
        println "DummyJenkinsfile.node(${nodeName})"
        closure()
    }
    public node(Closure closure) {
        println "DummyJenkinsfile.node { }"
        closure()
    }
    public deleteDir() { println "DummyJenkinsfile.deleteDir" }
    public checkout(scm) { println "DummyJenkinsfile.checkout(${scm})" }
    public withEnv(Collection env, Closure closure) {
        println "DummyJenkinsfile.withEnv(${env})"
        closure()
    }
    public stage(String stageName, Closure closure) {
        println "DummyJenkinsfile.stage(${stageName})"
        closure()
    }
    public timeout(options, Closure closure) {
        println "DummyJenkinsfile.timeout(${options})"
        closure()
    }
    public input(options) {
        println "DummyJenkinsfile.input(${options})"
    }
    public ApplyJenkinsfileClosure(Closure closure) {
        println "DummyJenkinsfile.ApplyJenkinsfileClosure"
        closure.delegate = this
        closure()
    }
    public stash(args) {
        println "DummyJenkinsfile.stash(${args})"
    }
    public unstash(args) {
        println "DummyJenkinsfile.unstash(${args})"
    }

    public dir(String directory, Closure closure) {
        println "DummyJenkinsfile.dir(${directory})"
        closure.delegate = this
        closure()
    }

    public resolveScm(Map args) {
        println "DummyJenkinsfile.resolveScm(${args})"
    }
}
