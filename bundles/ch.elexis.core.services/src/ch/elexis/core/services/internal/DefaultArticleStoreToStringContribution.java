package ch.elexis.core.services.internal;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStoreToStringContribution;

@Component
public class DefaultArticleStoreToStringContribution implements IStoreToStringContribution {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Override
	public Optional<String> storeToString(Identifiable identifiable){
		return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Optional<Identifiable> loadFromString(String storeToString){
		if (storeToString == null) {
			LoggerFactory.getLogger(getClass()).warn("StoreToString is null");
			return Optional.empty();
		}
		
		if (storeToString.startsWith("ch.elexis.medikamente")) {
			String[] split = splitIntoTypeAndId(storeToString);
			if (split != null && split.length == 2) {
				return (Optional<Identifiable>) (Optional<?>) coreModelService.load(split[1],
					IArticle.class, true);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Class<?> getEntityForType(String type){
		// no mapping available for default articles
		return null;
	}
	
	@Override
	public String getTypeForEntity(Object entityInstance){
		// no mapping available for default articles
		return null;
	}
	
	@Override
	public String getTypeForModel(Class<?> interfaze){
		// no mapping available for default articles
		return null;
	}
}
