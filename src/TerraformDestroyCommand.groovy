class TerraformDestroyCommand extends TerraformApplyCommand{

    private String command
    String environment

    public TerraformDestroyCommand(String environment) {
        this.environment = environment
        this.command = "destroy"
    }

    @Override
    public static TerraformDestroyCommand instanceFor(String environment) {
        return new TerraformDestroyCommand(environment)
            .withInput(false)
            .withArgument("-auto-approve")
    }

}
