package ch.elexis.core.jpa.liquibase;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.osgi.framework.Bundle;

import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;

public class LiquibaseClassResolver extends DefaultPackageScanClassResolver {
	private final Bundle bundle;

	private HashMap<String, Set<String>> packageToFile = new HashMap<String, Set<String>>();

	public LiquibaseClassResolver(Bundle bundle) {
		this.bundle = bundle;
		addJar(bundle.getEntry("/lib/liquibase-3.6.1.jar"));
		addJar(bundle.getEntry("/lib/snakeyaml-1.18.jar"));
	}

	@Override
	protected void find(final PackageScanFilter test, final String packageName, final Set<Class<?>> classes) {
		Set<String> classNames = getClasses(packageName, true);
		for (String className : classNames) {
			try {
				Class<?> klass = bundle.loadClass(className);
				if (test.matches(klass)) {
					classes.add(klass);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param jarUrl
	 *            The jar file to add.
	 */
	public final void addJar(final URL jarUrl) {
		if (jarUrl != null) {
			JarInputStream jis = null;
			try {
				jis = new JarInputStream(jarUrl.openStream());
				JarEntry next;
				while ((next = jis.getNextJarEntry()) != null) {
					String name = next.getName();
					if (name.endsWith(".class")) {
						String fixedName = name.substring(0, name.indexOf('.')).replace('/', '.');
						String packageName = fixedName.substring(0, name.lastIndexOf('.'));
						Set<String> classes = packageToFile.get(packageName);
						if (classes == null) {
							classes = new HashSet<String>();
							packageToFile.put(packageName, classes);
						}
						classes.add(fixedName);
					}
				}
			} catch (IOException e) {

			} finally {
				if (jis != null) {
					try {
						jis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @param packageName
	 *            The package to search.
	 * @param searchSubPackages
	 *            true to also include sub-packages.
	 * @return a list of class names.
	 */
	public final Set<String> getClasses(final String packageName, final boolean searchSubPackages) {
		Set<String> found;
		if (searchSubPackages) {
			found = new HashSet<String>();
			for (Entry<String, Set<String>> entry : packageToFile.entrySet()) {
				if (entry.getKey().startsWith(packageName)) {
					found.addAll(entry.getValue());
				}
			}
		} else {
			found = packageToFile.get(packageName);
			if (found == null) {
				found = new HashSet<String>();
			}
		}
		return found;
	}
}
