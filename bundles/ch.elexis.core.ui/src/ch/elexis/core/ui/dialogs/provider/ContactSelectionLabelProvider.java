package ch.elexis.core.ui.dialogs.provider;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;

public class ContactSelectionLabelProvider extends DefaultLabelProvider {
	
	private static DateTimeFormatter dateOfBirthFormatter =
		DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	@Override
	public String getText(Object element){
		if (element instanceof IContact) {
			IContact contact = (IContact) element;
			
			String label = contact.getLabel();
			if (contact.isPerson()) {
				Optional<IPerson> person =
					CoreModelServiceHolder.get().load(contact.getId(), IPerson.class);
				String dateOfBirthString = (person.get().getDateOfBirth() != null
						? person.get().getDateOfBirth().format(dateOfBirthFormatter)
						: "?");
				label = label + " (" + dateOfBirthString + ")";
			}
			if (contact.isUser()) {
				label = StringUtils.defaultString(contact.getDescription1()) + " "
					+ StringUtils.defaultString(contact.getDescription2()) + " - " + label;
			}
			return label;
		} else if (element instanceof Identifiable) {
			return ((Identifiable) element).getLabel();
		}
		return element.toString();
	}
	
	public String getColumnText(Object element, int columnIndex){
		return getText(element);
	}
}
