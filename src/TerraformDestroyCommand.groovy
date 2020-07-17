class TerraformDestroyCommand extends TerraformApplyCommand{

    private String command = "destroy"

    public TerraformDestroyCommand(String environment) {
        super(environment)
    }

}
