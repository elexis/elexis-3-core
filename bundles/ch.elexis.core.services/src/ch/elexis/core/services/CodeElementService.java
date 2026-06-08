package ch.elexis.core.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Component
public class CodeElementService implements ICodeElementService {

	@Inject
	@All
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
	volatile List<ICodeElementServiceContribution> _contributions;

	@Override
	public Optional<ICodeElement> loadFromString(String system, String code, Map<Object, Object> context) {
		Optional<ICodeElementServiceContribution> contribution = findContributionBySystem(system);
		if (contribution.isPresent()) {
			if (context == null) {
				context = Collections.emptyMap();
			}
			return contribution.get().loadFromCode(code, context);
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("No ICodeElementServiceContribution for system [" + system + "] code [" + code + "]");
		}
		return Optional.empty();
	}

	private Optional<ICodeElementServiceContribution> findContributionBySystem(String system) {
		return _contributions.stream().filter(e -> e.getSystem().equalsIgnoreCase(system)).findFirst();
	}

	@Override
	public Optional<IArticle> findArticleByGtin(String gtin) {
		for (ICodeElementServiceContribution contribution : getContributionsByTyp(CodeElementTyp.ARTICLE)) {
			Optional<ICodeElement> loadFromCode = contribution.loadFromCode(gtin);
			if (loadFromCode.isPresent()) {
				if (loadFromCode.get() instanceof IArticle) {
					return loadFromCode.map(IArticle.class::cast);
				} else {
					LoggerFactory.getLogger(getClass()).warn(
							"Found article for gtin [{}] but is not castable to IArticle [{}]", gtin,
							loadFromCode.get().getClass().getName());
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public List<ICodeElementServiceContribution> getContributionsByTyp(CodeElementTyp typ) {
		return _contributions.stream().filter(contribution -> contribution.getTyp() == typ)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<ICodeElementServiceContribution> getContribution(CodeElementTyp typ, String codeSystemName) {
		return findContributionBySystem(codeSystemName.toLowerCase());
	}

	@Override
	public Map<Object, Object> createContext() {
		HashMap<Object, Object> ret = new HashMap<>();
		Optional<IEncounter> consultation = PortableServiceLoader.get(IContextService.class).getRootContext()
				.getTyped(IEncounter.class);
		if (consultation.isPresent()) {
			ret.put(ContextKeys.CONSULTATION, consultation.get());
			ret.put(ContextKeys.COVERAGE, consultation.get().getCoverage());
		}
		if (ret.get(ContextKeys.COVERAGE) == null) {
			Optional<ICoverage> coverage = PortableServiceLoader.get(IContextService.class).getRootContext()
					.getTyped(ICoverage.class);
			if (coverage.isPresent()) {
				ret.put(ContextKeys.COVERAGE, coverage.get());
			}
		}
		return ret;
	}

	@Override
	public Map<Object, Object> createContext(IEncounter encounter) {
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
