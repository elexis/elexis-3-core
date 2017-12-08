package ch.elexis.core.ui.importer.div.importers;

import java.util.List;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.importer.div.importers.IPersistenceHandler;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.IPatient;
import ch.elexis.data.LabOrder;
import ch.elexis.data.Patient;

public class PersistenceHandler implements IPersistenceHandler {
	
	@Override
	public List<ILabOrder> getLabOrdersByOrderId(String resultObj){
		return LabOrder.getLabOrdersByOrderId(resultObj);
	}

	public IPatient loadPatient(String id){
		Patient load = Patient.load(id);
		if(load!=null) {
			return new ContactBean(load);
		}
		return null;
	}
	
}
