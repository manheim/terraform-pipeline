class DestroyPlugin {

    public static void init() {
        //DestroyPlugin plugin = new DestroyPlugin()

        TerraformEnvironmentStage.withStrategy(new DestroyStrategy())
    }

    //@Override
    //public void apply(TerraformEnvironmentStage stage) {
    //    stage.withStrategy(new DestroyStrategy())
    //}

}
