class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin {
    public static void init() {
        TerraformEnvironmentStage.addPlugin(new FlywayMigrationPlugin())
    }

    public void apply(TerraformEnvironmentStage stage) {
        stage.decorate(TerraformEnvironmentStage.PLAN, flywayInfoClosure())
    }

    public Closure flywayInfoClosure() {
        return { innerClosure ->
            innerClosure()
            sh "echo run flyway info"
        }
    }
}
