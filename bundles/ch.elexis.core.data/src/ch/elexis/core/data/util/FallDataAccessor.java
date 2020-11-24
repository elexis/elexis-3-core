package ch.elexis.core.data.util;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

public class FallDataAccessor implements IDataAccess {
	private static final String KOSTENTRAEGER_UMLAUT = Fall.FLD_EXT_KOSTENTRAEGER;
	private static final String KOSTENTRAEGER_KUERZEL_UMLAUT = "KostenträgerKürzel";
	private static final String KOSTENTRAEGER_ORT_UMLAUT = "KostenträgerOrt";
	
	private static final String KOSTENTRAEGER = "Kostentraeger";
	private static final String KOSTENTRAEGER_KUERZEL = "KostentraegerKuerzel";
	private static final String KOSTENTRAEGER_ORT = "KostentraegerOrt";
	
	private static final String ARBEITGEBER = "Arbeitgeber";
	private static final String ARBEITGEBER_KUERZEL = "ArbeitgeberKuerzel";
	private static final String ZUWEISER = "Zuweiser";
	private static final String ZUWEISER_KUERZEL = "ZuweiserKuerzel";
	
	ArrayList<Element> elementsList;
	private Element[] elements = {
		new Element(IDataAccess.TYPE.STRING, KOSTENTRAEGER_UMLAUT, "[Fall:-:-:Kostentraeger]", null,
			0),
		new Element(IDataAccess.TYPE.STRING, KOSTENTRAEGER_KUERZEL_UMLAUT,
			"[Fall:-:-:KostentraegerKuerzel]", null, 0),
		new Element(IDataAccess.TYPE.STRING, KOSTENTRAEGER_ORT_UMLAUT,
			"[Fall:-:-:KostentraegerOrt]", null, 0),
		new Element(IDataAccess.TYPE.STRING, ARBEITGEBER, "[Fall:-:-:Arbeitgeber]", null, 0),
		new Element(IDataAccess.TYPE.STRING, ARBEITGEBER_KUERZEL, "[Fall:-:-:ArbeitgeberKuerzel]",
			null, 0),
		new Element(IDataAccess.TYPE.STRING, ZUWEISER, "[Fall:-:-:Zuweiser]", null, 0), new Element(
			IDataAccess.TYPE.STRING, ZUWEISER_KUERZEL, "[Fall:-:-:ZuweiserKuerzel]", null, 0)
	};
	
	public FallDataAccessor(){
		// initialize the list of defined elements
		elementsList = new ArrayList<Element>();
		for (int i = 0; i < elements.length; i++)
			elementsList.add(elements[i]);
	}
	
	@Override
	public String getName(){
		return "Fall erweitert";
	}
	
	@Override
	public String getDescription(){
		return "Fall erweitert";
	}
	
	@Override
	public List<Element> getList(){
		return elementsList;
	}
	
	@Override
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject,
		String dates, String[] params){
		Result<Object> result = new Result<Object>(""); //$NON-NLS-1$
		
		Fall fall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		Kontakt costBearer = fall.getCostBearer();
		
		if (costBearer != null) {
			if (descriptor.equalsIgnoreCase(KOSTENTRAEGER)
				|| descriptor.equalsIgnoreCase(KOSTENTRAEGER_UMLAUT)) {
				// WARN does not seem to be called anymore - see FallKostentraegerResolver
				result = new Result<Object>(costBearer.getPostAnschrift(true));
				
			} else if (descriptor.equalsIgnoreCase(KOSTENTRAEGER_KUERZEL)
				|| descriptor.equalsIgnoreCase(KOSTENTRAEGER_KUERZEL_UMLAUT)) {
				String label = costBearer.getLabel();
				String fullName = label.substring(0, label.indexOf(","));
				result = new Result<Object>(fullName);
				
			} else if (descriptor.equalsIgnoreCase(KOSTENTRAEGER_ORT)
				|| descriptor.equalsIgnoreCase(KOSTENTRAEGER_ORT_UMLAUT)) {
				result = new Result<Object>(costBearer.getAnschrift().getOrt());
			}
		}
		if (descriptor.equalsIgnoreCase(ARBEITGEBER)) {
			Kontakt contact = fall.getRequiredContact(ARBEITGEBER);
			if (contact != null && contact.exists()) {
				result = new Result<Object>(contact.getPostAnschrift(true));
			}
		} else if (descriptor.equalsIgnoreCase(ARBEITGEBER_KUERZEL)) {
			Kontakt contact = fall.getRequiredContact(ARBEITGEBER);
			if (contact != null && contact.exists()) {
				String label = contact.getLabel();
				String fullName = label.substring(0, label.indexOf(","));
				result = new Result<Object>(fullName);
			}
		}
		if (descriptor.equalsIgnoreCase(ZUWEISER)) {
			Kontakt contact = fall.getRequiredContact(ZUWEISER);
			if (contact != null && contact.exists()) {
				result = new Result<Object>(contact.getPostAnschrift(true));
			}
		} else if (descriptor.equalsIgnoreCase(ZUWEISER_KUERZEL)) {
			Kontakt contact = fall.getRequiredContact(ZUWEISER);
			if (contact != null && contact.exists()) {
				String label = contact.getLabel();
				String fullName = label.substring(0, label.indexOf(","));
				result = new Result<Object>(fullName);
			}
		}
		return result;
	}
}
