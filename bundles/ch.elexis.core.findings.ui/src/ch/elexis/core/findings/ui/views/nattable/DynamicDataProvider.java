package ch.elexis.core.findings.ui.views.nattable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.model.IPatient;

public class DynamicDataProvider implements IDataProvider {

	private List<IFinding> currentFindings;

	private List<ICoding> shownCodings;
	private List<LocalDate> shownDates;
	private HashMap<LocalDate, List<IFinding>[]> shownFindings;

	private boolean rowsAreDates;

	public DynamicDataProvider() {
		currentFindings = new ArrayList<>();
		shownFindings = new HashMap<>();
		shownDates = new ArrayList<>();
		shownCodings = new ArrayList<>();
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (rowsAreDates) {
			if (columnIndex >= 0 && rowIndex >= 0 && rowIndex < shownDates.size()) {
				List<IFinding>[] findings = shownFindings.get(shownDates.get(rowIndex));
				if (findings != null && columnIndex < findings.length) {
					if (findings[columnIndex] != null) {
						return findings[columnIndex];
					}
				}
			}
		} else {
			if (columnIndex >= 0 && rowIndex >= 0 && columnIndex < shownDates.size()) {
				List<IFinding>[] findings = shownFindings.get(shownDates.get(columnIndex));
				if (findings != null && rowIndex < findings.length) {
					if (findings[rowIndex] != null) {
						return findings[rowIndex];
					}
				}
			}
		}
		return null;
	}

	@Override
	public int getRowCount() {
		if (rowsAreDates) {
			return shownDates.size();
		} else {
			return shownCodings.size();
		}
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		// ignore, no edit here ...
	}

	@Override
	public int getColumnCount() {
		if (rowsAreDates) {
			return shownCodings.size();
		} else {
			return shownFindings.size();
		}
	}

	public void reload(IPatient selectedPatient) {
		if (selectedPatient == null) {
			currentFindings.clear();
		} else {
			currentFindings = getFindings(selectedPatient);
		}
		updateShownFindings();
	}

	public List<IFinding> getFindings(IPatient patient) {
		List<IFinding> ret = new ArrayList<>();
		if (patient != null) {
			String patientId = patient.getId();
			ret.addAll(getObservations(patientId));
			/*
			 * TODO currently only observations needed
			 * items.addAll(getConditions(patientId));
			 * items.addAll(getClinicalImpressions(patientId));
			 * items.addAll(getPrecedureRequest(patientId));
			 */
		}
		return ret;
	}

	private List<IObservation> getObservations(String patientId) {
		return FindingsServiceComponent.getService().getPatientsFindings(patientId, IObservation.class);
	}

	public void setShownCodings(List<ICoding> showCodings) {
		this.shownCodings = showCodings;
		updateShownFindings();
	}

	@SuppressWarnings("unchecked")
	private void updateShownFindings() {
		shownFindings.clear();
		shownDates.clear();

		for (IFinding iFinding : currentFindings) {
			if (iFinding instanceof IObservation) {
				IObservation iObservation = (IObservation) iFinding;
				int index = getCodingIndex(iObservation);
				if (index != -1) {
					LocalDate date = iObservation.getEffectiveTime().orElse(LocalDateTime.MIN).toLocalDate();
					List<IFinding>[] findings = shownFindings.get(date);
					if (findings == null) {
						findings = (List<IFinding>[]) new List[shownCodings.size()];
						shownDates.add(date);
					}
					if (findings[index] == null) {
						findings[index] = new ArrayList<>();
					}
					findings[index].add(iObservation);
					shownFindings.put(date, findings);
				}
			}
		}
		Collections.sort(shownDates);
		Collections.reverse(shownDates);
	}

	private int getCodingIndex(IObservation iObservation) {
		for (ICoding iCoding : iObservation.getCoding()) {
			for (int i = 0; i < shownCodings.size(); i++) {
				if (shownCodings.get(i).getCode().equals(iCoding.getCode())) {
					return i;
				}
			}
		}
		return -1;
	}

	public List<LocalDate> getShownDates() {
		return shownDates;
	}

	public List<ICoding> getShownCodings() {
		return shownCodings;
	}

	public void setRowsAreDates(boolean value) {
		this.rowsAreDates = value;
	}

	public boolean isRowsAreDates() {
		return this.rowsAreDates;
	}
}
