package ch.elexis.core.data.service.internal;

import java.util.HashMap;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.ICodeElement;
import ch.elexis.core.data.services.ICodeElementService;
import ch.elexis.core.data.services.ICodeElementServiceContribution;

/**
 * 
 * @deprecated use implementation of {@link ch.elexis.core.services.ICodeElementService} instead.
 *
 */
@Component
public class PoCodeElementService implements ICodeElementService {
	
	private HashMap<String, ICodeElementServiceContribution> contributions = new HashMap<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setCodeElementServiceContribution(ICodeElementServiceContribution contribution){
		ICodeElementServiceContribution previous =
			contributions.put(contribution.getSystem(), contribution);
		if (previous != null) {
			LoggerFactory.getLogger(getClass())
				.warn("Possible ICodeElementServiceContribution collision previous [" + previous
					+ "] new [" + contribution + "]");
		}
	}
	
	public void unsetCodeElementServiceContribution(ICodeElementServiceContribution store){
		contributions.remove(store.getSystem());
	}
	
	@Override
	public Optional<ICodeElement> createFromString(String system, String code,
		HashMap<Object, Object> context){
		ICodeElementServiceContribution contribution = contributions.get(system);
		if (contribution != null) {
			return contribution.createFromCode(code, context);
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("No ICodeElementServiceContribution for system [" + system + "] code [" + code
					+ "]");
		}
		return Optional.empty();
	}
}
