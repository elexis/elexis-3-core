package ch.elexis.core.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.localservice.Constants;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;

@Component
public class EigenleistungCodeElementService implements ICodeElementServiceContribution {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Override
	public String getSystem() {
		return Constants.TYPE_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context) {
		INamedQuery<ICustomService> query = coreModelService.getNamedQuery(ICustomService.class, "code");
		List<ICustomService> found = query.executeWithParameters(query.getParameterMap("code", code));
		if (!found.isEmpty()) {
			if (found.size() > 1) {
				LoggerFactory.getLogger(getClass())
						.warn("Found more than one " + getSystem() + " with code [" + code + "] using first");
			}
			return Optional.of(found.get(0));
		} else {
			return (Optional<ICodeElement>) (Optional<?>) coreModelService.load(code, ICustomService.class);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context) {
		return (List<ICodeElement>) (List<?>) coreModelService.getQuery(ICustomService.class).execute();
	}

	@Override
	public CodeElementTyp getTyp() {
		return CodeElementTyp.SERVICE;
	}
}
