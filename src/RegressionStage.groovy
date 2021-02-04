class RegressionStage implements Stage, DecoratableStage, Resettable {

    public String testCommand
    public List automationRepoList = []
    private String testCommandDirectory
    private Jenkinsfile jenkinsfile

    private StageDecorations decorations

    private static plugins = []

    public RegressionStage() {
        this("./bin/test.sh")
    }

    public RegressionStage(String testCommand) {
        this.testCommand = testCommand
        this.jenkinsfile = Jenkinsfile.instance
        this.decorations = new StageDecorations()
    }

    public RegressionStage withScm(String automationRepo) {
        this.automationRepoList.add(automationRepo)
        return this
    }

    public RegressionStage changeDirectory(String directory) {
        this.testCommandDirectory = directory
        return this
    }

    public Stage then(Stage nextStage) {
        return new BuildGraph(this).then(nextStage)
    }

    public void build() {
        Jenkinsfile.build(pipelineConfiguration())
    }

    private Closure pipelineConfiguration() {
        applyPlugins()

        return {
            node(jenkinsfile.getNodeName()) {
                stage("test") {
                    decorations.apply {
                        if (automationRepoList.isEmpty()) {
                            checkout scm
                            sh testCommand
                        } else if (automationRepoList.size() == 1) {
                            checkout resolveScm(source: [$class: 'GitSCMSource', remote: automationRepoList.first(), traits: [[$class: 'jenkins.plugins.git.traits.BranchDiscoveryTrait'], [$class: 'LocalBranchTrait']]], targets: [BRANCH_NAME, 'master'])
                            sh testCommand
                        } else {
                            for (url in automationRepoList) {
                                def dirName = url.split('/').last() - '.git'
                                dir(dirName) {
                                    checkout resolveScm(source: [$class: 'GitSCMSource', remote: url, traits: [[$class: 'jenkins.plugins.git.traits.BranchDiscoveryTrait'], [$class: 'LocalBranchTrait']]], targets: [BRANCH_NAME, 'master'])
                                }
                            }

                            if (this.testCommandDirectory) {
                                dir(this.testCommandDirectory) {
                                    sh testCommand
                                }
                            } else {
                                sh testCommand
                            }
                        }
                    }
                }
            }
        }
    }

    public void decorate(Closure decoration) {
        decorations.add(decoration)
    }

    public static getPlugins() {
        return plugins
    }

    public static void reset() {
        this.plugins = []
    }

    public static void addPlugin(plugin) {
        plugins << plugin
    }

    public void applyPlugins() {
        for (plugin in plugins) {
            plugin.apply(this)
        }
    }

}
