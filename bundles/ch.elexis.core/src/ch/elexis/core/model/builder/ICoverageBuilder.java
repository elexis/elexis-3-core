package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

public class ICoverageBuilder {
	
	public static class Builder extends AbstractBuilder<ICoverage> {
		public Builder(IModelService modelService, IPatient patient, String label, String reason,
			String billingSystem){
			super(modelService);
			object = modelService.create(ICoverage.class);
			object.setPatient(patient);
			object.setDescription(label);
			object.setReason(reason);
			object.setDateFrom(LocalDate.now());
			object.setBillingSystem(billingSystem);
		}
	}
	
}
