package ch.elexis.core.importer.div.tasks.internal;

import java.util.List;

import org.slf4j.Logger;

import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class HL7ImporterLabContactResolver implements ILabContactResolver {
	
	private Logger logger;
	private IModelService coreModelService;
	private boolean bCreateLaboratoryIfNotExists;
	
	public HL7ImporterLabContactResolver(IModelService coreModelService, Logger logger,
		boolean bCreateLaboratoryIfNotExists){
		this.coreModelService = coreModelService;
		this.logger = logger;
		this.bCreateLaboratoryIfNotExists = bCreateLaboratoryIfNotExists;
	}
	
	@Override
	public ILaboratory getLabContact(String identifier, String sendingFacility){
		
		ILaboratory laboratory;
		
		IQuery<ILaboratory> query = coreModelService.getQuery(ILaboratory.class);
		query.and(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, "%" + identifier + "%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE,
			"%" + identifier + "%");
		List<ILaboratory> results = query.execute();
		if (results.isEmpty()) {
			if (!bCreateLaboratoryIfNotExists) {
				logger.warn("Found no Labor for identifier [{}]. Automatic creation deactivated.",
					identifier);
				return null;
			}
			logger.info("Found no Labor for identifier [{}]. Created new Labor contact.",
				identifier);
			laboratory =
				new IContactBuilder.LaboratoryBuilder(coreModelService, identifier).buildAndSave();
		} else {
			laboratory = results.get(0);
			if (results.size() > 1) {
				logger.warn(
					"Found more than one Labor for identifier [{}]. This can cause problems when importing results.",
					identifier);
			}
		}
		
		return laboratory;
	}
	
}
