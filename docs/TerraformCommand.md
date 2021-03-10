`TerraformCommand` is a trait that can be implemented by a class that wraps a
Terraform command. The trait has one required method:

* `String assembleCommandString()`: This method should return the Terraform
  command to be executed with any arguments or flags included.

The trait provides two parameters:

* `environment`: The TerraformEnvironmentStage name for an instance of the
  Command class
* `terraformBinary` (Default: `terraform`): Convenience property to allow
  overriding the terraform command to run. The `assembleCommandString()`
  method can refer to this property when assembling the final command, or
  ignore it entirely.

```
class MyTerraformCommand implements TerraformCommand {
  public String assembleCommandString() {
    return terraformBinary + " help"
  }
}