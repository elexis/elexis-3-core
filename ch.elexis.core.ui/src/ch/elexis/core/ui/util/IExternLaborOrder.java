package ch.elexis.core.ui.util;

import java.util.List;

import ch.elexis.core.data.LabOrder;
import ch.elexis.core.data.Patient;

public interface IExternLaborOrder {
	public void order(Patient patient, List<LabOrder> orders);
	
	public String getLabel();
}
