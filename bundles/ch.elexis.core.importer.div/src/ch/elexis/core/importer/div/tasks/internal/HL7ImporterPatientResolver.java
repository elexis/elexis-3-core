package ch.elexis.core.importer.div.tasks.internal;

import org.slf4j.Logger;

import ch.elexis.core.importer.div.importers.AbstractHL7PatientResolver;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

class HL7ImporterPatientResolver extends AbstractHL7PatientResolver {
	
	private IModelService coreModelService;
	private Logger logger;
	
	public HL7ImporterPatientResolver(IModelService coreModelService, Logger logger){
		this.coreModelService = coreModelService;
		this.logger = logger;
	}
	
	@Override
	public IPatient resolvePatient(String firstname, String lastname, String birthDate,
		String sender){
		// TODO
		return null;
	}
	
}
