class TerraformDestroyCommand extends TerraformApplyCommand {

    public TerraformDestroyCommand(String environment) {
        this.environment = environment
        this.command = "destroy"
    }

}
