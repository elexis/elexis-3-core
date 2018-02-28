package ch.elexis.core.findings.ui.views.nattable;

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
import ch.elexis.data.Patient;

public class DynamicDataProvider implements IDataProvider {
	
	private List<IFinding> currentFindings;
	
	private List<ICoding> shownCodings;
	private List<LocalDateTime> shownDates;
	private HashMap<LocalDateTime, List<IFinding>[]> shownFindings;
	
	public DynamicDataProvider(){
		currentFindings = new ArrayList<>();
		shownFindings = new HashMap<>();
		shownDates = new ArrayList<>();
		shownCodings = new ArrayList<>();
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex){
		if (rowIndex >= 0 && rowIndex < shownDates.size()) {
			List<IFinding>[] findings = shownFindings.get(shownDates.get(rowIndex));
			if (findings != null && columnIndex < findings.length) {
				if (findings[columnIndex] != null) {
					return findings[columnIndex];
				}
			}
		}
		return null;
	}
	
	@Override
	public int getRowCount(){
		return shownFindings.size();
	}
	
	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue){
		// ignore, no edit here ...
	}
	
	@Override
	public int getColumnCount(){
		return shownCodings.size();
	}
	
	public void reload(Patient selectedPatient){
		if (selectedPatient == null) {
			currentFindings.clear();
		} else {
			currentFindings = getFindings(selectedPatient);
		}
		updateShownFindings();
	}
	
	public List<IFinding> getFindings(Patient patient){
		List<IFinding> ret = new ArrayList<>();
		if (patient != null && patient.exists()) {
			String patientId = patient.getId();
			ret.addAll(getObservations(patientId));
			/*	TODO currently only observations needed
			 * items.addAll(getConditions(patientId));
				items.addAll(getClinicalImpressions(patientId));
				items.addAll(getPrecedureRequest(patientId));
				*/
		}
		return ret;
	}
	
	private List<IObservation> getObservations(String patientId){
		return FindingsServiceComponent.getService().getPatientsFindings(patientId,
			IObservation.class);
	}
	
	public void setShownCodings(List<ICoding> showCodings){
		this.shownCodings = showCodings;
		updateShownFindings();
	}
	
	@SuppressWarnings("unchecked")
	private void updateShownFindings(){
		shownFindings.clear();
		shownDates.clear();
		
		for (IFinding iFinding : currentFindings) {
			if (iFinding instanceof IObservation) {
				IObservation iObservation = (IObservation) iFinding;
				int index = getCodingIndex(iObservation);
				if (index != -1) {
					LocalDateTime time = iObservation.getEffectiveTime().orElse(LocalDateTime.MIN);
					List<IFinding>[] findings = shownFindings.get(time);
					if (findings == null) {
						findings = (List<IFinding>[]) new List[shownCodings.size()];
						shownDates.add(time);
					}
					if (findings[index] == null) {
						findings[index] = new ArrayList<>();
					}
					findings[index].add(iObservation);
					shownFindings.put(time, findings);
				}
			}
		}
		Collections.sort(shownDates);
		Collections.reverse(shownDates);
	}
	
	private int getCodingIndex(IObservation iObservation){
		for (ICoding iCoding : iObservation.getCoding()) {
			for (int i = 0; i < shownCodings.size(); i++) {
				if (shownCodings.get(i).getCode().equals(iCoding.getCode())) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public List<LocalDateTime> getShownDates(){
		return shownDates;
	}
}
