package ch.elexis.scripting.beanshell.internal;

import java.util.List;

public class MultiClassLoader extends ClassLoader {
	
	private List<ClassLoader> loaders;
	
	public MultiClassLoader(List<ClassLoader> loaders){
		this.loaders = loaders;
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException{
		// try to load the class with the ClassLoaders
		Class<?> clazz = null;
		try {
			clazz = super.loadClass(name);
		} catch (ClassNotFoundException e) {
			for (ClassLoader loader : loaders) {
				try {
					clazz = loader.loadClass(name);
				} catch (ClassNotFoundException ex) {
					// ignore
				}
				// class loaded
				if (clazz != null) {
					break;
				}
			}
		}
		if (clazz == null) {
			throw new ClassNotFoundException(name);
		}
		return clazz;
	}
	
	public void addClassLoader(ClassLoader classLoader){
		if (!loaders.contains(classLoader)) {
			loaders.add(classLoader);
		}
	}
}
