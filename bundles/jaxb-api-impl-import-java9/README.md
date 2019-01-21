
# JAXB-API import package binding for Java > 8

This fragment binds to jaxb-api, importing for Java > 8 the
`com.sun.xml.bind.v2`  package from `com.sun.xml.bind.jaxb-osgi` which
provides a concrete JAXB implementation.

Without this fragment, instantiations of JAXB would face the following exception:

	Caused by: javax.xml.bind.JAXBException: Implementation of JAXB-API has not been found on module path or classpath.
    at javax.xml.bind.ContextFinder.newInstance(ContextFinder.java:269) ~[na:na]
    at javax.xml.bind.ContextFinder.find(ContextFinder.java:412) ~[na:na]
    at javax.xml.bind.JAXBContext.newInstance(JAXBContext.java:721) ~[na:na]
    at javax.xml.bind.JAXBContext.newInstance(JAXBContext.java:662) ~[na:na]
    at at.medevit.redmine.rest.messages.UserBodyReader.readFrom(UserBodyReader.java:40) ~[na:na]
    ... 93 common frames omitted
	Caused by: java.lang.ClassNotFoundException: com.sun.xml.bind.v2.ContextFactory
    at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:583) ~[na:na]
    at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:178) ~[na:na]
    at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:521) ~[na:na]
    at org.eclipse.osgi.internal.framework.ContextFinder.loadClass(ContextFinder.java:135) ~[na:na]
    at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:521) ~[na:na]
    at javax.xml.bind.ServiceLoaderUtil.nullSafeLoadClass(ServiceLoaderUtil.java:122) ~[na:na]
    at javax.xml.bind.ServiceLoaderUtil.safeLoadClass(ServiceLoaderUtil.java:155) ~[na:na]
    at javax.xml.bind.ContextFinder.newInstance(ContextFinder.java:267) ~[na:na]
    ... 97 common frames omitted
