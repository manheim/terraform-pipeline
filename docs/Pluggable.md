The `Pluggable` trait can be used to add plugin management to a class. It
takes as a type parameter the plugin type it accepts.

It adds several new methods.

* `addPlugin(T plugin)`, which accepts a plugin of the
  appropriate type and adds it to the list of plugins.
* `applyPlugins()` takes no parameters. It assures that all plugins are
  applied, and are applied at most once. It can be called as many times as desired.

```
class MyTerraformCommand implements TerraformCommand, Pluggable<MyTerraformCommandPlugin>
```