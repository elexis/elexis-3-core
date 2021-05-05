package ch.elexis.core.model.builder;

import java.time.LocalDate;
import java.util.Arrays;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.BillingSystem;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IBillingSystemService;
import ch.elexis.core.services.IConfigService;
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
		// only to transport the name of the IBillingSystem,
		// not validating whether it exists
		object.setBillingSystem(new BillingSystem(billingSystem, null));
	}
	
	/**
	 * Build a new {@link ICoverage}, derive coverage label, reason and billingSystem via the
	 * required services
	 * 
	 * @param modelService
	 * @param configService
	 * @param billingSystemService
	 * @param patient
	 */
	public ICoverageBuilder(IModelService modelService, IConfigService configService,
		IBillingSystemService billingSystemService, IPatient patient){
		super(modelService);
		this.patient = patient;
		
		object = modelService.create(ICoverage.class);
		object.setPatient(patient);
		object.setDateFrom(LocalDate.now());
		
		String label = getDefaultCoverageLabel(configService);
		object.setDescription(label);
		String reason = getDefaultCoverageReason(configService);
		object.setReason(reason);
		
		String billingSystem = getDefaultCoverageLaw(configService, billingSystemService);
		// only to transport the name of the IBillingSystem,
		// not validating whether it exists
		object.setBillingSystem(new BillingSystem(billingSystem, null));
	}
	
	@Override
	public ICoverage buildAndSave(){
		modelService.save(Arrays.asList(object, patient));
		return object;
	}
	
	/**
	 * 
	 * @param configService
	 * @return the default coverage label as defined by the user
	 */
	public String getDefaultCoverageLabel(IConfigService configService){
		return configService.getActiveUserContact(Preferences.USR_DEFCASELABEL,
			Preferences.USR_DEFCASELABEL_DEFAULT);
	}
	
	/**
	 * 
	 * @param configService
	 * @return the default coverage reason as defined by the user
	 */
	public String getDefaultCoverageReason(IConfigService configService){
		return configService.getActiveUserContact(Preferences.USR_DEFCASEREASON,
			Preferences.USR_DEFCASEREASON_DEFAULT);
	}
	
	/**
	 * 
	 * @param configService
	 * @param billingSystemService
	 * @return the default billing system law
	 */
	public static String getDefaultCoverageLaw(IConfigService configService,
		IBillingSystemService billingSystemService){
		return configService.getActiveUserContact(Preferences.USR_DEFLAW,
			billingSystemService.getDefaultBillingSystem().getLaw().name());
	}
	
}
