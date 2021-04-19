class MockWorkflowScript {
    public docker
    public scm
    public env = [:]
    public static BRANCH_NAME

    public MockWorkflowScript() {
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
    public echo(String message) { println "MockWorkflowScript.echo ${message}" }
    public pwd(Map options) { return '/MockWorkflowScript/currentDir' }
    public writeFile(Map options) { println "MockWorkflowScript.writeFile: ${options.toString()}" }
    public readFile(String filename) { return "MockWorkflowScript.readFile(${filename}): some content" }
    public fileExists(String filename) { return false }
    public sh(String command) { println "MockWorkflowScript.sh: ${command.toString()}"; return 'MockWorkflowScript.sh output' }
    public sh(Map options) { println "MockWorkflowScript.sh: ${options.toString()}"; return 'MockWorkflowScript.sh output' }
    public string(Map options) {  println "MockWorkflowScript.string: ${options.toString()}" }
    public parameters(params) {  println "MockWorkflowScript.parameters: ${params.toString()}" }
    public properties(props) {  println "MockWorkflowScript.properties: ${props.toString()}" }
    public node(String nodeName, Closure closure) {
        println "MockWorkflowScript.node(${nodeName})"
        closure()
    }
    public node(Closure closure) {
        println "MockWorkflowScript.node { }"
        closure()
    }
    public deleteDir() { println "MockWorkflowScript.deleteDir" }
    public checkout(scm) { println "MockWorkflowScript.checkout(${scm})" }
    public withEnv(Collection env, Closure closure) {
        println "MockWorkflowScript.withEnv(${env})"
        closure()
    }
    public stage(String stageName, Closure closure) {
        println "MockWorkflowScript.stage(${stageName})"
        closure()
    }
    public timeout(options, Closure closure) {
        println "MockWorkflowScript.timeout(${options})"
        closure()
    }
    public input(options) {
        println "MockWorkflowScript.input(${options})"
    }
    public ApplyJenkinsfileClosure(Closure closure) {
        println "MockWorkflowScript.ApplyJenkinsfileClosure"
        closure.delegate = this
        closure()
    }
    public stash(args) {
        println "MockWorkflowScript.stash(${args})"
    }
    public unstash(args) {
        println "MockWorkflowScript.unstash(${args})"
    }

    public dir(String directory, Closure closure) {
        println "MockWorkflowScript.dir(${directory})"
        closure.delegate = this
        closure()
    }

    public resolveScm(Map args) {
        println "MockWorkflowScript.resolveScm(${args})"
    }

    public ansiColor(String arg, Closure closure) {
        print "MockWorkflowScript.ansiColor(${arg})"
        closure.delegate = this
        closure()
    }
}

