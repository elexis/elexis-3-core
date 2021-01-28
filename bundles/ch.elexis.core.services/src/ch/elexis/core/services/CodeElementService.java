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

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICodeElement;

@Component
public class CodeElementService implements ICodeElementService {
	
	private HashMap<String, ICodeElementServiceContribution> contributions = new HashMap<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
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
	public Optional<ICodeElement> loadFromString(String system, String code,
		Map<Object, Object> context){
		ICodeElementServiceContribution contribution = contributions.get(system);
		if (contribution != null) {
			if (context == null) {
				context = Collections.emptyMap();
			}
			return contribution.loadFromCode(code, context);
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("No ICodeElementServiceContribution for system [" + system + "] code [" + code
					+ "]");
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<IArticle> findArticleByGtin(String gtin){
		for (ICodeElementServiceContribution contribution : getContributionsByTyp(
			CodeElementTyp.ARTICLE)) {
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
	public List<ICodeElementServiceContribution> getContributionsByTyp(CodeElementTyp typ){
		return contributions.values().stream().filter(contribution -> contribution.getTyp() == typ)
			.collect(Collectors.toList());
	}
	
	@Override
	public Optional<ICodeElementServiceContribution> getContribution(CodeElementTyp typ,
		String codeSystemName){
		return Optional.ofNullable(contributions.get(codeSystemName));
	}
}
