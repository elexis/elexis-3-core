package ch.elexis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IVerify;
import ch.elexis.core.model.IVerifyContext;
import ch.elexis.core.model.IVerifyConverter;

public class VerifyContext implements IVerifyContext {
	
	private List<IVerify> errors = new ArrayList<>();
	private List<IVerify> items = new ArrayList<>();
	private Map<String, String> info = new HashMap<>();
	
	private IVerifyConverter iVerifyConverter;
	
	public static VerifyContext create(Konsultation konsultation,
		IVerifyConverter converter){
		VerifyContext ret = new VerifyContext();
		ret.setIVerifyConverter(converter);
		
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
			IVerify verify = converter.convert(leistung.getVerrechenbar()).get();
			verify.setCount(leistung.getZahl() * leistung.getSecondaryScaleFactor());
			
			String[] res = new String[2];
			leistung.get(new String[] {
				Verrechnet.CLASS, Verrechnet.LEISTG_CODE
			}, res);
			
			verify.getInfo().put("dbSeite", leistung.getDetail("Seite"));
			verify.getInfo().put("dbClass", res[0]);
			verify.getInfo().put("dbLeistGCode", res[1]);
			ret.items.add(verify);
		}
		
		return ret;
	}
	
	@Override
	public List<IVerify> getErrors(){
		return errors;
	}
	
	@Override
	public List<IVerify> getItems(){
		return items;
	}
	
	@Override
	public IVerifyConverter getIVerifyConverter(){
		return iVerifyConverter;
	}
	
	@Override
	public void setIVerifyConverter(IVerifyConverter value){
		this.iVerifyConverter = value;
		
	}
	
	public Map<String, String> getInfo(){
		return info;
	}
}