package ch.elexis.core.ui.mediorder;

import java.time.format.DateTimeFormatter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.model.IStock;

public class MediorderStockFilter extends ViewerFilter {
	private String searchTerm;
	private DateTimeFormatter dateFormatter;

	public void setSearchTerm(String term) {
		this.searchTerm = ".*" + term.toLowerCase() + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchTerm == null || searchTerm.length() == 0) {
			return true;
		}

		IStock stock = (IStock) element;
		String patientId = stock.getOwner().asIPatient().getPatientNr().toLowerCase();
		if (patientId.matches(searchTerm)) {
			return true;
		}

		String firstName = stock.getOwner().getFirstName().toLowerCase();
		if (firstName.matches(searchTerm)) {
			return true;
		}

		String lastName = stock.getOwner().getLastName().toLowerCase();
		if (lastName.matches(searchTerm)) {
			return true;
		}

		dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		String birthDate = stock.getOwner().getDateOfBirth().format(dateFormatter).toLowerCase();
		if (birthDate.matches(searchTerm)) {
			return true;
		}

		return false;
	}

}