class BuildGraph implements Stage {
    private List<Stage> stages

    public BuildGraph(Stage start) {
        this.stages = [start]
    }

    public Stage then(Stage nextStage) {
        this.stages << nextStage
        return this
    }

    public void build() {
        Jenkinsfile.build(stages)
    }
}
