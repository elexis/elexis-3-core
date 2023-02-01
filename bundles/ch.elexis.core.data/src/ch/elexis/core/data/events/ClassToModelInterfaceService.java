package ch.elexis.core.data.events;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ClassToModelInterfaceService.class)
public class ClassToModelInterfaceService {

	@Reference
	public List<ElexisClassToModelInterfaceContribution> contributions;

	public Optional<Class<?>> getCoreModelInterfaceForElexisClass(Class<?> elexisClazz) {
		if (contributions != null && !contributions.isEmpty()) {
			for (ElexisClassToModelInterfaceContribution elexisClassToModelInterfaceContribution : contributions) {
				Optional<Class<?>> ret = elexisClassToModelInterfaceContribution
						.getCoreModelInterfaceForElexisClass(elexisClazz);
				if (ret.isPresent()) {
					return ret;
				}
			}
		}
		return Optional.empty();
	}
}
