package ch.elexis.core.services.holder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;

@Component
public class CodeElementServiceHolder {
	private static ICodeElementService codeElementService;
	
	@Reference
	public void setContextService(ICodeElementService codeElementService){
		CodeElementServiceHolder.codeElementService = codeElementService;
	}
	
	public static ICodeElementService get(){
		return codeElementService;
	}
	
	/**
	 * Create a default context using the current typed selection of encounter and coverage from the
	 * {@link ContextServiceHolder}s root context.
	 * 
	 * @return
	 */
	public static Map<Object, Object> createContext(){
		HashMap<Object, Object> ret = new HashMap<>();
		Optional<IEncounter> consultation =
			ContextServiceHolder.get().getRootContext().getTyped(IEncounter.class);
		if (consultation.isPresent()) {
			ret.put(ContextKeys.CONSULTATION, consultation.get());
		}
		Optional<ICoverage> coverage =
			ContextServiceHolder.get().getRootContext().getTyped(ICoverage.class);
		if (coverage.isPresent()) {
			ret.put(ContextKeys.COVERAGE, coverage.get());
		}
		return ret;
	}
	
	/**
	 * Create a context using the provided encounter.
	 * 
	 * @param encounter
	 * @return
	 */
	public static Map<Object, Object> createContext(IEncounter encounter){
		HashMap<Object, Object> ret = new HashMap<>();
		if (encounter != null) {
			ret.put(ContextKeys.CONSULTATION, encounter);
			ICoverage coverage = encounter.getCoverage();
			if (coverage != null) {
				ret.put(ContextKeys.COVERAGE, coverage);
			}
		}
		return ret;
	}
}
