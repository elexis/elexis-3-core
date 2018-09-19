package ch.elexis.core.model.builder;

import java.time.LocalDate;
import java.util.Arrays;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

public class ICoverageBuilder extends AbstractBuilder<ICoverage> {
	
	private IPatient patient;
	
	public ICoverageBuilder(IModelService modelService, IPatient patient, String label,
		String reason, String billingSystem){
		super(modelService);
		this.patient = patient;
		
		object = modelService.create(ICoverage.class);
		object.setPatient(patient);
		object.setDescription(label);
		object.setReason(reason);
		object.setDateFrom(LocalDate.now());
		object.setBillingSystem(billingSystem);
		
		patient.addCoverage(object);
	}
	
	@Override
	public ICoverage buildAndSave(){
		modelService.save(Arrays.asList(object, patient));
		return object;
	}
	
}
