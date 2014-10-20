package ch.elexis.core.ui.dialogs.provider;

import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Kontakt;
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
			return label;
		}
		return element.toString();
	}
	
	public String getColumnText(Object element, int columnIndex){
		return getText(element);
	}
	
}
