package ch.elexis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.BillingVerification;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IVerificationContext;
import ch.elexis.core.model.IVerificationConverter;

public class BillingVerificationContext implements IVerificationContext<BillingVerification> {
	
	private List<BillingVerification> errors = new ArrayList<>();
	private List<BillingVerification> items = new ArrayList<>();
	private Map<String, String> info = new HashMap<>();
	
	private IVerificationConverter<IBillable, BillingVerification> iVerificationConverter;
	
	public static BillingVerificationContext create(Konsultation konsultation,
		IVerificationConverter<IBillable, BillingVerification> converter){
		BillingVerificationContext ret = new BillingVerificationContext();
		ret.setiVerificationConverter(converter);
		
		Mandant mandant = konsultation.getMandant();
		Fall fall = konsultation.getFall();
		Patient patient = fall.getPatient();
		Rechnungssteller rechnungssteller = fall.getRechnungssteller();
		
		if (patient != null) {
			ret.getInfo().put("patBirthdate", patient.getGeburtsdatum());
			ret.getInfo().put("patSex", patient.getGeschlecht());
		}
		
		if (rechnungssteller != null) {
			ret.getInfo().put("physicianEan", rechnungssteller.getXid(XidConstants.EAN));
			
		}
		if (mandant != null) {
			ret.getInfo().put("treatmentEan", mandant.getXid(XidConstants.EAN));
		
			ret.getInfo().put("treatmentCanton",
				String.valueOf(mandant.getExtInfoStoredObjectByKey("Kanton")));
			
		}
		
		String gesetz = fall.getRequiredString("Gesetz");
		ret.getInfo().put("gesetz", String.valueOf(gesetz));
		
		for (Verrechnet leistung : konsultation.getLeistungen()) {
			BillingVerification billingVerification =
				converter.convert(leistung.getVerrechenbar()).get();
			billingVerification.setCount(leistung.getZahl() * leistung.getSecondaryScaleFactor());
			
			String[] res = new String[2];
			leistung.get(new String[] {
				Verrechnet.CLASS, Verrechnet.LEISTG_CODE
			}, res);
			
			billingVerification.getInfo().put("dbSeite", leistung.getDetail("Seite"));
			billingVerification.getInfo().put("dbClass", res[0]);
			billingVerification.getInfo().put("dbLeistGCode", res[1]);
			ret.items.add(billingVerification);
		}
		
		return ret;
	}
	
	@Override
	public List<BillingVerification> getErrors(){
		return errors;
	}
	
	@Override
	public List<BillingVerification> getItems(){
		return items;
	}
	
	public Map<String, String> getInfo(){
		return info;
	}
	
	public void setiVerificationConverter(
		IVerificationConverter<IBillable, BillingVerification> iVerificationConverter){
		this.iVerificationConverter = iVerificationConverter;
	}
	
	public IVerificationConverter<IBillable, BillingVerification> getiVerificationConverter(){
		return iVerificationConverter;
	}
}