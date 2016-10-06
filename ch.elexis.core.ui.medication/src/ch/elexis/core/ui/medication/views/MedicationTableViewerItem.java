package ch.elexis.core.ui.medication.views;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.Anwender;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.rgw.tools.TimeTool;

/**
 * Maps an element of type {@link Prescription} for presentation within the MedicationTableViewer.
 * Used for performance reasons.
 */
public class MedicationTableViewerItem {
	
	// loaded first run
	private Prescription prescription;
	private String artikelsts;
	private String dateFrom;
	private String dateUntil;
	private String dosis;
	private String bemerkung;
	private String rezeptId;
	private String sortOrder;
	private String prescriptorId;
	private long lastUpdate;
	
	// lazy computed
	private String artikelLabel;
	private Object suppliedUntil;
	private Object lastDisposed;
	
	private Date endTime;
	
	public MedicationTableViewerItem(Prescription p){
		String[] values =
			p.get(false, Prescription.FLD_ARTICLE, Prescription.FLD_DOSAGE, Prescription.FLD_REMARK,
				Prescription.FLD_REZEPT_ID, Prescription.FLD_LASTUPDATE,
				Prescription.FLD_SORT_ORDER, Prescription.FLD_PRESCRIPTOR);
		prescription = p;
		artikelsts = values[0];
		dosis = values[1];
		bemerkung = values[2];
		rezeptId = values[3];
		lastUpdate = (values[4] != null && values[4].length() > 0) ? Long.valueOf(values[4]) : 0l;
		sortOrder = values[5];
		prescriptorId = values[6];
		
		dateFrom = p.getBeginDate();
		dateUntil = p.getEndDate();
		
		endTime = new TimeTool(prescription.getEndDate()).getTime();
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
	
	public String getDisposalComment(){
		return prescription.getDisposalComment();
	}
	
	public String getBeginDate(){
		return dateFrom;
	}
	
	public String getEndDate(){
		return dateUntil;
	}
	
	public Date getEndTime(){
		return endTime;
	}
	
	public void setEndTime(Date time){
		endTime = time;
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
			IPersistentObject ld = prescription.getLastDisposed(rezeptId);
			if (ld == null) {
				lastDisposed = StringConstants.EMPTY;
			} else {
				lastDisposed = ld;
			}
		}
		return (lastDisposed instanceof IPersistentObject) ? (IPersistentObject) lastDisposed
				: null;
	}
	
	public TimeTool getSuppliedUntilDate(){
		if (suppliedUntil == null) {
			TimeTool suppliedUntilDate = prescription.getSuppliedUntilDate();
			if (suppliedUntilDate == null) {
				suppliedUntil = StringConstants.EMPTY;
			} else {
				suppliedUntil = suppliedUntilDate;
			}
		}
		return (suppliedUntil instanceof TimeTool) ? (TimeTool) suppliedUntil : null;
	}
	
	public EntryType getEntryType(){
		return prescription.getEntryType();
	}
	
	public boolean isReserveMedication(){
		return prescription.isReserveMedication();
	}
	
	public String getArtikelLabel(){
		if (artikelLabel == null) {
			Artikel artikel = (Artikel) CoreHub.poFactory.createFromString(artikelsts);
			if (artikel == null) {
				artikel = prescription.getArtikel();
			}
			
			if (artikel == null) {
				artikelLabel = "?";
			} else {
				artikelLabel = artikel.getLabel();
			}
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
	
	public boolean isStopped(){
		String endTime = prescription.getEndDate();
		return !endTime.isEmpty();
	}
	
	public Optional<Anwender> getPrescriptor(){
		if (prescriptorId != null && !prescriptorId.isEmpty()) {
			Anwender prescriptor = Anwender.load(prescriptorId);
			if (prescriptor != null && prescriptor.exists()) {
				return Optional.of(prescriptor);
			}
		}
		return Optional.empty();
	}
}