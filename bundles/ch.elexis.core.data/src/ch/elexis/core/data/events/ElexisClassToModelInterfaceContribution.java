package ch.elexis.core.data.events;

import java.util.Optional;

public interface ElexisClassToModelInterfaceContribution {

	public Optional<Class<?>> getCoreModelInterfaceForElexisClass(Class<?> elexisClazz);

}
