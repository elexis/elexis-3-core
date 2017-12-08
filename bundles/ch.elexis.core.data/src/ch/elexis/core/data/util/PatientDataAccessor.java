package ch.elexis.core.data.util;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

public class PatientDataAccessor implements IDataAccess {
	private static final String GESETZVERTRETER_UMLAUT = "Gesetzlicher Vertreter";
	private static final String GESETZVERTRETER_KUERZEL_UMLAUT = "Gesetzlicher Vertreter Kürzel";
	private static final String GESETZVERTRETER_ORT_UMLAUT = "Gesetzlicher Vertreter Ort";
	
	private static final String STAMMARZT_UMLAUT = "Stammarzt";
	private static final String STAMMARZT_KUERZEL_UMLAUT = "Stammarzt Kürzel";
	private static final String STAMMARZT_ORT_UMLAUT = "Stammarzt Ort";
	
	private static final String GESETZVERTRETER = "GesetzVertreter";
	private static final String GESETZVERTRETER_KUERZEL = "GesetzVertreterKuerzel";
	private static final String GESETZVERTRETER_ORT = "GesetzVertreterOrt";
	
	private static final String STAMMARZT = "Stammarzt";
	private static final String STAMMARZT_KUERZEL = "StammarztKuerzel";
	private static final String STAMMARZT_ORT = "StammarztOrt";
	
	ArrayList<Element> elementsList;
	private Element[] elements = {
		new Element(IDataAccess.TYPE.STRING, GESETZVERTRETER_UMLAUT,
			"[Patient:-:-:GesetzVertreter]", null, 0),
		new Element(IDataAccess.TYPE.STRING, GESETZVERTRETER_KUERZEL_UMLAUT,
			"[Patient:-:-:GesetzVertreterKuerzel]", null, 0),
		new Element(IDataAccess.TYPE.STRING, GESETZVERTRETER_ORT_UMLAUT,
			"[Patient:-:-:GesetzVertreterOrt]", null, 0),
		new Element(IDataAccess.TYPE.STRING, STAMMARZT_UMLAUT, "[Patient:-:-:Stammarzt]", null, 0),
		new Element(IDataAccess.TYPE.STRING, STAMMARZT_KUERZEL_UMLAUT,
			"[Patient:-:-:StammarztKuerzel]", null, 0),
		new Element(IDataAccess.TYPE.STRING, STAMMARZT_ORT_UMLAUT, "[Patient:-:-:StammarztOrt]",
			null, 0)
	};
	
	public PatientDataAccessor(){
		// initialize the list of defined elements
		elementsList = new ArrayList<Element>();
		for (int i = 0; i < elements.length; i++)
			elementsList.add(elements[i]);
	}
	
	@Override
	public String getName(){
		return "Patient erweitert";
	}
	
	@Override
	public String getDescription(){
		return "Patient erweitert";
	}
	
	@Override
	public List<Element> getList(){
		return elementsList;
	}
	
	@Override
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject,
		String dates, String[] params){
		Result<Object> result = new Result<Object>("");
		
		Patient patient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		
		if (patient != null) {
			// gesetzlicher Vertreter
			if (descriptor.equalsIgnoreCase(GESETZVERTRETER)
				|| descriptor.equalsIgnoreCase(GESETZVERTRETER_UMLAUT)) {
				Kontakt legalGuardian = getLegalGuardian(patient);
				result = new Result<Object>(legalGuardian.getPostAnschrift(true));
			} else if (descriptor.equalsIgnoreCase(GESETZVERTRETER_KUERZEL)
				|| descriptor.equalsIgnoreCase(GESETZVERTRETER_KUERZEL_UMLAUT)) {
				Kontakt legalGuardian = getLegalGuardian(patient);
				String label = legalGuardian.getLabel();
				String fullName = label.substring(0, label.indexOf(","));
				result = new Result<Object>(fullName);
			} else if (descriptor.equalsIgnoreCase(GESETZVERTRETER_ORT)
				|| descriptor.equalsIgnoreCase(GESETZVERTRETER_ORT_UMLAUT)) {
				Kontakt legalGuardian = getLegalGuardian(patient);
				result = new Result<Object>(legalGuardian.getAnschrift().getOrt());
			}
			
			if (descriptor.equalsIgnoreCase(STAMMARZT)
				|| descriptor.equalsIgnoreCase(STAMMARZT_UMLAUT)) {
				Kontakt familyDoctor = getFamilyDoctor(patient);
				if (familyDoctor != null) {
					result = new Result<Object>(familyDoctor.getPostAnschrift(true));
				}
			} else if (descriptor.equalsIgnoreCase(STAMMARZT_KUERZEL)
				|| descriptor.equalsIgnoreCase(STAMMARZT_KUERZEL_UMLAUT)) {
				Kontakt familyDoctor = getFamilyDoctor(patient);
				if (familyDoctor != null) {
					String label = familyDoctor.getLabel();
					String fullName = label.substring(0, label.indexOf(","));
					result = new Result<Object>(fullName);
				}
			} else if (descriptor.equalsIgnoreCase(STAMMARZT_ORT)
				|| descriptor.equalsIgnoreCase(STAMMARZT_ORT_UMLAUT)) {
				Kontakt familyDoctor = getFamilyDoctor(patient);
				if (familyDoctor != null) {
					result = new Result<Object>(familyDoctor.getAnschrift().getOrt());
				}
			}
		}
		return result;
	}
	
	private Kontakt getLegalGuardian(Patient patient){
		Kontakt legalGuardian = patient.getLegalGuardian();
		if (legalGuardian == null) {
			legalGuardian = patient;
		}
		return legalGuardian;
	}
	
	private Kontakt getFamilyDoctor(Patient patient){
		return patient.getStammarzt();
	}
}
