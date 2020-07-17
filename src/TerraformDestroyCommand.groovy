class TerraformDestroyCommand extends TerraformApplyCommand{

    public TerraformDestroyCommand(String environment, String command = "destroy") {
        super(environment, command)
    }

    @Override
    public static TerraformDestroyCommand instanceFor(String environment) {
        return new TerraformDestroyCommand(environment)
            .withInput(false)
            .withArgument("-auto-approve")
    }

}
