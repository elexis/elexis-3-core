# Portable Service Loader Implementation

As described in [maven-packages](https://github.com/elexis/elexis-3-core/blob/master/maven-packages.md "title") the Elexis
bundles should become less technological dependent. One major hurdle to re-use the bundles is the dependency injection.

This bundle aims to provide a portable means to load services, both in Eclipse OSGi and Quarkus Arc.

### Quarkus Integration

In order for `PortableServiceLoader` to be activated, somebody has to inject it. The other approach
would be to add the `io.quarkus.Startup` annotation to the class, but this is not done at the moment.