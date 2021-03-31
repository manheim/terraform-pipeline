class FlywayMigrationPlugin implements TerraformEnvironmentStagePlugin {
    public static void init() {
        TerraformEnvironmentStage.addPlugin(new FlywayMigrationPlugin())
    }

    public void apply(TerraformEnvironmentStage stage) {
        println "do the thing"
    }
}
