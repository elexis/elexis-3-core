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
	
	ArrayList<Element> elementsList;
	private Element[] elements = {
		new Element(IDataAccess.TYPE.STRING, KOSTENTRAEGER_UMLAUT, "[Fall:-:-:Kostentraeger]", null,
			0),
		new Element(IDataAccess.TYPE.STRING, KOSTENTRAEGER_KUERZEL_UMLAUT,
			"[Fall:-:-:KostentraegerKuerzel]", null, 0),
		new Element(IDataAccess.TYPE.STRING, KOSTENTRAEGER_ORT_UMLAUT,
			"[Fall:-:-:KostentraegerOrt]", null, 0)
	};
	
	public FallDataAccessor(){
		// initialize the list of defined elements
		elementsList = new ArrayList<Element>();
		for (int i = 0; i < elements.length; i++)
			elementsList.add(elements[i]);
	}
	
	@Override
	public String getName(){
		return "Fall Kostenträger";
	}
	
	@Override
	public String getDescription(){
		return "Kostenträger eines Falles";
	}
	
	@Override
	public List<Element> getList(){
		return elementsList;
	}
	
	@Override
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject,
		String dates, String[] params){
		Result<Object> result = null;
		
		Fall fall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		Kontakt costBearer = fall.getCostBearer();
		
		if (descriptor.equalsIgnoreCase(KOSTENTRAEGER)
			|| descriptor.equalsIgnoreCase(KOSTENTRAEGER_UMLAUT)) {
			result = new Result<Object>(costBearer.getPostAnschrift(true));
			
		} else if (descriptor.equalsIgnoreCase(KOSTENTRAEGER_KUERZEL)
			|| descriptor.equalsIgnoreCase(KOSTENTRAEGER_KUERZEL_UMLAUT)) {
			String label = costBearer.getLabel();
			String fullName = label.substring(0, label.indexOf(","));
			result = new Result<Object>(fullName);
			
		} else if (descriptor.equalsIgnoreCase(KOSTENTRAEGER_ORT)
			|| descriptor.equalsIgnoreCase(KOSTENTRAEGER_ORT_UMLAUT)) {
			result = new Result<Object>(costBearer.getAnschrift().getOrt());
			
		} else {
			result = new Result<Object>(Result.SEVERITY.ERROR, IDataAccess.OBJECT_NOT_FOUND,
				"Kein Kostenträger gefunden", //$NON-NLS-1$
				null, false);
		}
		return result;
	}
}
