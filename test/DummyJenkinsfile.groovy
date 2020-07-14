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
}
