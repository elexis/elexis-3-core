package ch.elexis.core.importer.div.tasks.internal;

import java.util.List;

import org.slf4j.Logger;

import ch.elexis.core.importer.div.importers.IContactResolver;
import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.importer.div.importers.ILabImportUtil;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class HL7ImporterLabContactResolver implements ILabContactResolver {

	private Logger logger;
	private IModelService coreModelService;
	private ILabImportUtil labImportUtil;
	private boolean bCreateLaboratoryIfNotExists;

	public HL7ImporterLabContactResolver(IModelService coreModelService, ILabImportUtil labImportUtil, Logger logger,
			boolean bCreateLaboratoryIfNotExists) {
		this.coreModelService = coreModelService;
		this.labImportUtil = labImportUtil;
		this.logger = logger;
		this.bCreateLaboratoryIfNotExists = bCreateLaboratoryIfNotExists;
	}

	@Override
	public ILaboratory getLabContact(String identifier, String sendingFacility) {
		return labImportUtil.getLinkLabor(sendingFacility, new MyContactResolver(identifier));
	}

	private class MyContactResolver implements IContactResolver<ILaboratory> {

		private final String identifier;

		public MyContactResolver(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public ILaboratory getContact(String message) {
			if (identifier == null) {
				logger.warn("identifier is null");
				return null;
			}

			ILaboratory laboratory = null;
			IQuery<ILaboratory> query = coreModelService.getQuery(ILaboratory.class);
			query.and(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, "%" + identifier + "%");
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%" + identifier + "%");
			List<ILaboratory> results = query.execute();
			if (results.isEmpty()) {
				if (!bCreateLaboratoryIfNotExists) {
					logger.warn("Found no Labor for identifier [{}]. Automatic creation deactivated.", identifier);
					return null;
				}
				logger.info("Found no Labor for identifier [{}]. Created new Labor contact.", identifier);
				laboratory = new IContactBuilder.LaboratoryBuilder(coreModelService, identifier).buildAndSave();
			} else {
				if (results.size() == 1) {
					return results.get(0);
				} else {
					for (ILaboratory lab : results) {
						if (lab.getCode().equalsIgnoreCase(identifier.trim())) {
							return lab;
						}
					}
					laboratory = results.get(0);
					logger.warn("Found more than one Labor for identifier [{}] but no exact match, returning [{}]",
							identifier, laboratory.getCode());
				}
			}

			return laboratory;
		}

	}

}
