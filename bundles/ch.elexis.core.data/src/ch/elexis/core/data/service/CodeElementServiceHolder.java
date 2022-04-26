package ch.elexis.core.data.service;

import java.util.HashMap;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.services.IContextService;
import ch.elexis.data.Konsultation;

@Component(service = {})
public class CodeElementServiceHolder {

	private static HashMap<Object, Object> emptyMap = new HashMap<>();

	private static ICodeElementService elementService;

	@Reference(unbind = "-")
	public void setCodeElementService(ICodeElementService elementService) {
		CodeElementServiceHolder.elementService = elementService;
	}

	public static ICodeElementService get() {
		return elementService;
	}

	/**
	 * Create a context map using the selection of {@link IContextService}.
	 *
	 * @return
	 */
	public static HashMap<Object, Object> createContext() {
		HashMap<Object, Object> ret = new HashMap<>();
		Optional<IEncounter> consultation = ContextServiceHolder.get().getRootContext().getTyped(IEncounter.class);
		if (consultation.isPresent()) {
			ret.put(ContextKeys.CONSULTATION, consultation.get());
		}
		Optional<ICoverage> coverage = ContextServiceHolder.get().getRootContext().getTyped(ICoverage.class);
		if (coverage.isPresent()) {
			ret.put(ContextKeys.COVERAGE, coverage.get());
		}
		return ret;
	}

	/**
	 * Create a context map using the provided {@link Konsultation}.
	 *
	 * @param consultation
	 * @return
	 */
	public static HashMap<Object, Object> createContext(Konsultation consultation) {
		HashMap<Object, Object> ret = new HashMap<>();
		if (consultation != null) {
			IEncounter encounter = NoPoUtil.loadAsIdentifiable(consultation, IEncounter.class).get();
			ret.put(ContextKeys.CONSULTATION, encounter);
			ret.put(ContextKeys.COVERAGE, encounter.getCoverage());
		}
		return ret;
	}

	public static HashMap<Object, Object> emtpyContext() {
		return emptyMap;
	}
}
