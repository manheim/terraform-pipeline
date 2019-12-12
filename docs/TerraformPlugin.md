## [TerraformPlugin](../src/TerraformPlugin.groovy)

This plugin provides a code point for the library to apply different arguments
and behaviors based on your expected terraform version.

### Usage

This is a default plugin that is automatically added to all required commands
and stages.  As such, there is no `init()` method to call on the class.

Instead, place a `.terraform-version` file in the root of your repository 
containing the version of terraform you require.

If this is not possible, you may declare the version in your Jenkinsfile as
follows:

```
// Jenkinsfile

Jenkinsfile.init(this)

TerraformPlugin.withVersion('0.12.17')
```

### Development

When a version change occurs that breaks with previous behavior, you will need
to update this plugin to account for that.  The process is fairly
straightforward.  Please see documentation in the
[source code](../src/TerraformPlugin.groovy) for the plugin for further
information.
