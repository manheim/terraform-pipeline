class RegressionStage implements Stage {

    public String testCommand
    public List automationRepoList = []
    private String testCommandDirectory

    private Closure existingDecorations

    private static plugins = []

    public RegressionStage() {
        this("./bin/test.sh")
    }

    public RegressionStage(String testCommand) {
        this.testCommand = testCommand
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
            node {
                stage("test") {
                    applyDecorations(delegate) {
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

    private void applyDecorations(delegate, Closure stageClosure) {
        if (existingDecorations != null) {
            existingDecorations.delegate = delegate
            existingDecorations(stageClosure)
        } else {
            stageClosure.delegate = delegate
            stageClosure()
        }
    }

    public static getPlugins() {
        return plugins
    }

    public static void resetPlugins() {
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

    public void decorate(Closure decoration) {
        if (existingDecorations == null) {
            existingDecorations = decoration
            existingDecorations.resolveStrategy = Closure.DELEGATE_FIRST
        } else {
            def newDecoration = { stage ->
                decoration.delegate = delegate
                decoration.resolveStrategy = Closure.DELEGATE_FIRST
                decoration() {
                    stage.delegate = delegate
                    existingDecorations.delegate = delegate
                    existingDecorations(stage)
                }
            }
            existingDecorations = newDecoration
        }
    }
}
