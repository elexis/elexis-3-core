package ch.elexis.core.eigenartikel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.eigenartikel.Constants;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.types.ArticleTyp;

@Component
public class EigenartikelCodeElementService implements ICodeElementServiceContribution {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Override
	public String getSystem(){
		return Constants.TYPE_NAME;
	}
	
	@Override
	public Optional<ICodeElement> createFromCode(String code, HashMap<Object, Object> context){
		INamedQuery<IArticle> query = coreModelService.getNamedQuery(IArticle.class, "typ", "code");
		
		List<IArticle> found = query.executeWithParameters(
			coreModelService.getParameterMap("typ", ArticleTyp.EIGENARTIKEL, "code", code));
		if (!found.isEmpty()) {
			if (found.size() > 1) {
				LoggerFactory.getLogger(getClass()).warn("Found more than one "
					+ ArticleTyp.EIGENARTIKEL.getCodeSystemName() + " with code [" + code
					+ "] using first");
			}
			return Optional.of(found.get(0));
		} else {
			query = coreModelService.getNamedQuery(IArticle.class, "typ", "id");
			found = query.executeWithParameters(
				coreModelService.getParameterMap("typ", ArticleTyp.EIGENARTIKEL, "id", code));
			if (!found.isEmpty()) {
				if (found.size() > 1) {
					LoggerFactory.getLogger(getClass())
						.warn("Found more than one " + ArticleTyp.EIGENARTIKEL.getCodeSystemName()
							+ " with id [" + code + "] using first");
				}
				return Optional.of(found.get(0));
			}
		}
		return Optional.empty();
	}
	
	@Override
	public CodeElementTyp getTyp(){
		return CodeElementTyp.ARTICLE;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ICodeElement> getElements(HashMap<Object, Object> context){
		INamedQuery<IArticle> query = coreModelService.getNamedQuery(IArticle.class, "typ");
		return (List<ICodeElement>) (List<?>) query.executeWithParameters(
			coreModelService.getParameterMap("typ", ArticleTyp.EIGENARTIKEL));
	}
}
