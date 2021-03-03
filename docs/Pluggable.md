The `Pluggable` trait can be used to add plugin management to a class. It
takes as a type parameter the plugin type it accepts.

It adds several new methods.

* `addPlugin(T plugin)`, which accepts a plugin of the
  appropriate type and adds it to the list of plugins.
* `applyPlugins()` takes no parameters. It assures that all plugins are
  applied, and are applied at most once. It can be called as many times as desired.

The trait also modifies the `toString()` method to apply the plugins prior to
passing the method call up to the next trait in the chain, or the base class
if it is the last trait in the chain.

**NOTE** Traits are chained right to left - so the last trait in any chain is
the first one declared in the `implements` clause.

```
class MyTerraformCommand implements TerraformCommand, Pluggable<MyTerraformCommandPlugin>
```