package ch.elexis.core.ui.mediorder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IStock;

public class MediorderFilter extends ViewerFilter {

	private String searchTerm;
	private DateTimeFormatter dateFormatter;

	public MediorderFilter() {
		dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	}

	public void setSearchTerm(String term) {
		this.searchTerm = ".*" + term.toLowerCase() + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchTerm == null || searchTerm.isEmpty()) {
			return true;
		}

		IStock stock = (IStock) element;
		IPatient patient = stock.getOwner().asIPatient();

		String id = patient.getPatientNr();
		if (id.matches(searchTerm)) {
			return true;
		}

		String firstName = patient.getDescription1().toLowerCase();
		if (firstName.matches(searchTerm)) {
			return true;
		}

		String lastName = patient.getDescription2().toLowerCase();
		if (lastName.matches(searchTerm)) {
			return true;
		}

		LocalDateTime birthDate = patient.getDateOfBirth();
		if (birthDate.format(dateFormatter).matches(searchTerm)) {
			return true;
		}

		return false;
	}

}
