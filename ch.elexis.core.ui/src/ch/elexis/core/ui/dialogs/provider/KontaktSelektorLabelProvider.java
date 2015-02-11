package ch.elexis.core.ui.dialogs.provider;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;

public class KontaktSelektorLabelProvider extends DefaultLabelProvider {
	@Override
	public String getText(Object element){
		if (element instanceof Kontakt) {
			Kontakt k = (Kontakt) element;
			
			String label = k.getLabel();
			if (k.istPerson()) {
				label = label + " (" + k.get(Person.BIRTHDATE) + ")";
			}
			if (StringConstants.ONE.equals(k.get(Kontakt.FLD_IS_USER))) {
				label = k.get(Kontakt.FLD_NAME1) + " " + k.get(Kontakt.FLD_NAME2) + " - " + label;
			}
			return label;
		} else if (element instanceof PersistentObject) {
			PersistentObject po = (PersistentObject) element;
			return po.getLabel();
		}
		return element.toString();
	}
	
	public String getColumnText(Object element, int columnIndex){
		return getText(element);
	}
	
}
