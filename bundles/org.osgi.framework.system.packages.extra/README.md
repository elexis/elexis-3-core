
# OSGi Extender Fragment


This fragment provides the `com.source.tree`  and `com.source.util`  packages to
`com.sun.xml.bind.jaxb-osgi`  for resolving the bundles during Tycho build, resp.
Eclipse feature bundling.

At runtime, these packages are resolved by `org.eclipse.osgi`.

See https://github.com/eclipse-ee4j/jaxb-api/issues/92 and
https://bugs.eclipse.org/bugs/show_bug.cgi?id=540426
