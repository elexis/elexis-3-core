package ch.elexis.core.ui.medication.views;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.TableViewer;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.rgw.tools.TimeTool;

/**
 * Maps an element of type {@link Prescription} for presentation within the MedicationTableViewer.
 * Used for performance reasons.
 */
public class MedicationTableViewerItem {
	
	private static TableViewer tv;
	private static ExecutorService executorService = Executors.newFixedThreadPool(10);
	
	// loaded first run
	private Prescription prescription;
	private String artikelsts;
	private String dateFrom;
	private String dateUntil;
	private String dosis;
	private String bemerkung;
	private String rezeptId;
	private String sortOrder;
	private long lastUpdate;
	
	// lazy computed
	private String artikelLabel;
	private Object suppliedUntil;
	private Object lastDisposed;
	
	public MedicationTableViewerItem(Prescription p){
		String[] values = p.get(false, Prescription.FLD_ARTICLE, Prescription.FLD_DATE_FROM,
			Prescription.FLD_DATE_UNTIL, Prescription.FLD_DOSAGE, Prescription.FLD_REMARK,
			Prescription.FLD_REZEPT_ID, Prescription.FLD_LASTUPDATE, Prescription.FLD_SORT_ORDER);
		prescription = p;
		artikelsts = values[0];
		dateFrom = values[1];
		dateUntil = values[2];
		dosis = values[3];
		bemerkung = values[4];
		rezeptId = values[5];
		lastUpdate = (values[6] != null && values[6].length() > 0) ? Long.valueOf(values[6]) : 0l;
		sortOrder = values[7];
	}
	
	public static void setTableViewer(TableViewer medicationTableViewer){
		MedicationTableViewerItem.tv = medicationTableViewer;
	}
	
	public static List<MedicationTableViewerItem> initFromPrescriptionList(
		List<Prescription> prescriptionList){
		List<MedicationTableViewerItem> collect = prescriptionList.stream()
			.map(p -> new MedicationTableViewerItem(p)).collect(Collectors.toList());
		return collect;
	}
	
	// loaded with first run
	public String getId(){
		return prescription.getId();
	}
	
	public String getBemerkung(){
		return bemerkung;
	}
	
	public void setBemerkung(String bemerkung){
		this.bemerkung = bemerkung;
		prescription.setBemerkung(bemerkung);
		tv.update(this, null);
	}
	
	public String getBeginDate(){
		return dateFrom;
	}
	
	public String getEndDate(){
		return dateUntil;
	}
	
	public String getDosis(){
		return dosis;
	}
	
	public Prescription getPrescription(){
		return prescription;
	}
	
	public long getLastUpdate(){
		return lastUpdate;
	}
	
	public String getArtikelsts(){
		return artikelsts;
	}
	///-------------------
	
	public String getStopReason(){
		// EXT!
		return prescription.getStopReason();
	}
	
	public void setStopReason(String stopReason){
		// EXT!
		prescription.setStopReason(stopReason);
	}
	
	public boolean isFixedMediation(){
		return prescription.isFixedMedication(rezeptId, dateUntil);
	}
	
	public IPersistentObject getLastDisposed(){
		if (lastDisposed == null) {
			lazyLoad(this, () -> {
				IPersistentObject ld = prescription.getLastDisposed(rezeptId);
				if (ld == null) {
					lastDisposed = StringConstants.EMPTY;
				} else {
					lastDisposed = ld;
				}
			});
			return null;
		}
		
		return (lastDisposed instanceof IPersistentObject) ? (IPersistentObject) lastDisposed
				: null;
	}
	
	public TimeTool getSuppliedUntilDate(){
		if (suppliedUntil == null) {
			lazyLoad(this, () -> {
				TimeTool suppliedUntilDate = prescription.getSuppliedUntilDate();
				if (suppliedUntilDate == null) {
					suppliedUntil = StringConstants.EMPTY;
				} else {
					suppliedUntil = suppliedUntilDate;
				}
			});
			return null;
		}
		return (suppliedUntil instanceof TimeTool) ? (TimeTool) suppliedUntil : null;
	}
	
	private void lazyLoad(MedicationTableViewerItem mtvi, Runnable r){
		executorService.execute(new Runnable() {
			public void run(){
				r.run();
				
				tv.getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run(){
						tv.update(mtvi, null);
					}
				});
			}
		});
	}
	
	public void addTerm(TimeTool begin, String newDose){
		String[] newValues = prescription.addTerm(begin, newDose);
		dateFrom = newValues[0];
		dosis = newValues[1];
		if (StringConstants.ZERO.equals(dosis)) {
			dateUntil = newValues[0];
		}
	}
	
	public EntryType getEntryType(){
		return prescription.getEntryType();
	}
	
	public boolean isReserveMedication(){
		return prescription.isReserveMedication();
	}
	
	public String getArtikelLabel(){
		if (artikelLabel == null) {
			lazyLoad(this, () -> {
				Artikel artikel = (Artikel) CoreHub.poFactory.createFromString(artikelsts);
				if (artikel == null) {
					artikel = prescription.getArtikel();
				}
				
				if (artikel == null) {
					artikelLabel = "?";
				} else {
					artikelLabel = artikel.getLabel();
				}
			});
			return "-";
		}
		return artikelLabel;
	}
	
	public void setOrder(String i){
		sortOrder = i;
		prescription.set(Prescription.FLD_SORT_ORDER, i);
	}
	
	public String getOrder(){
		return sortOrder;
	}
	
}
