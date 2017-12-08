package ch.elexis.core.scheduler.internal;

import org.quartz.simpl.CascadingClassLoadHelper;

public class ClassLoadHelper extends CascadingClassLoadHelper {
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException{
		try {
			return super.loadClass(name);
		} catch (ClassNotFoundException e) {
			// continue
		}
		
		return ElexisSchedulerExtensionPoint.getClassByName(name);
	}
}
