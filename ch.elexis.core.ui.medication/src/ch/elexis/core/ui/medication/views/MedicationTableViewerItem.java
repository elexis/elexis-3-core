package ch.elexis.core.ui.medication.views;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Anwender;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.data.User;
import ch.rgw.tools.TimeTool;

/**
 * Maps an element of type {@link Prescription} for presentation within the MedicationTableViewer.
 * Used for performance reasons.
 */
public class MedicationTableViewerItem {
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(8);
	
	private StructuredViewer viewer;
	
	// loaded first run
	private Prescription prescription;
	private String artikelId;
	private String artikelStoreToString;
	private String dateFrom;
	private String dateUntil;
	private String dosis;
	private String remark;
	private String disposalComment;
	private String rezeptId;
	private String sortOrder;
	private String prescriptorId;
	
	// lazy computed
	private String artikelLabel;
	private Object lastDisposed;
	private String prescriptorLabel;
	private String stopReason;
	private Image image;
	
	private boolean resolved = false;
	private boolean resolving = false;
	
	private Date endTime;
	
	private MedicationTableViewerItem(Prescription p, StructuredViewer viewer){
		this.viewer = viewer;
		String[] values = p.get(false, Prescription.FLD_ARTICLE_ID, Prescription.FLD_ARTICLE,
			Prescription.FLD_DOSAGE,
			Prescription.FLD_REMARK, Prescription.FLD_REZEPT_ID, Prescription.FLD_SORT_ORDER,
			Prescription.FLD_PRESCRIPTOR);
		prescription = p;
		artikelId = values[0];
		artikelStoreToString = values[1];
		dosis = values[2];
		remark = values[3];
		rezeptId = values[4];
		sortOrder = values[5];
		prescriptorId = values[6];
		
		dateFrom = p.getBeginTime();
		dateUntil = p.getEndTime();
		
		endTime = new TimeTool(dateUntil).getTime();
	}
	
	public static List<MedicationTableViewerItem> createFromPrescriptionList(
		List<Prescription> prescriptionList, StructuredViewer viewer){
		List<MedicationTableViewerItem> collect = prescriptionList.parallelStream()
			.map(p -> new MedicationTableViewerItem(p, viewer)).collect(Collectors.toList());
		return collect;
	}
	
	// loaded with first run
	public String getId(){
		return prescription.getId();
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
	
	public String getStopReason(){
		if (stopReason == null) {
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
		}
		return stopReason != null ? stopReason : "...";
	}
	
	public String getRemark(){
		return remark;
	}
	
	public String getDisposalComment(){
		if (disposalComment == null) {
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
		}
		return disposalComment != null ? disposalComment : "...";
	}
	
	public boolean isFixedMediation(){
		return prescription.isFixedMedication(rezeptId, dateUntil);
	}
	
	public IPersistentObject getLastDisposed(){
		if (lastDisposed == null) {
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
		}
		return (lastDisposed instanceof IPersistentObject) ? (IPersistentObject) lastDisposed
				: null;
	}
	
	public EntryType getEntryType(){
		return prescription.getEntryType();
	}
	
	public boolean isReserveMedication(){
		return prescription.isReserveMedication();
	}
	
	public String getArtikelLabel(){
		if (artikelLabel == null) {
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
		}
		return artikelLabel != null ? artikelLabel : "...";
	}
	
	public void setOrder(String i){
		sortOrder = i;
		prescription.set(Prescription.FLD_SORT_ORDER, i);
	}
	
	public String getOrder(){
		return sortOrder;
	}
	
	public boolean isStopped(){
		return dateUntil != null && !dateUntil.isEmpty();
	}
	
	public String getPrescriptorLabel(){
		if (prescriptorLabel == null) {
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
		}
		return prescriptorLabel != null ? prescriptorLabel : "...";
	}
	
	public Image getImage(){
		if (image == null) {
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
		}
		return image != null ? image : Images.IMG_EMPTY_TRANSPARENT.getImage();
	}
	
	/**
	 * Resolve the properties, blocks until resolved.
	 */
	public void resolve(){
		new ResolveLazyFieldsRunnable(null, this).run();
	}
	
	private static class ResolveLazyFieldsRunnable implements Runnable {
		private MedicationTableViewerItem item;
		private StructuredViewer viewer;
		
		public ResolveLazyFieldsRunnable(StructuredViewer viewer, MedicationTableViewerItem item){
			this.item = item;
			this.viewer = viewer;
		}
		
		@Override
		public void run(){
			resolveImage();
			resolveArticleLabel();
			resolveLastDisposed();
			resolveStopReason();
			resolvePrescriptorLabel();
			resolveDisposalComment();
			item.resolved = true;
			item.resolving = false;
			updateViewer();
		}
		
		private void updateViewer(){
			if (viewer != null) {
				Control control = viewer.getControl();
				if (control != null && !control.isDisposed()) {
					viewer.getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run(){
							if (!control.isDisposed() && control.isVisible()) {
								viewer.update(item, null);
							}
						}
					});
				}
			}
		}
		
		private void resolveArticleLabel(){
			if (item.artikelStoreToString != null && !item.artikelStoreToString.isEmpty()) {
				Artikel artikel =
					(Artikel) CoreHub.poFactory.createFromString(item.artikelStoreToString);
				if (artikel == null) {
					item.artikelLabel = "?";
				} else {
					item.artikelLabel = artikel.getLabel();
				}
			} else if (item.artikelId != null && !item.artikelId.isEmpty()) {
				Artikel artikel = Artikel.load(item.artikelId);
				if (artikel != null && artikel.exists()) {
					item.artikelLabel = artikel.getLabel();
				} else {
					item.artikelLabel = "?";
				}
			} else {
				item.artikelLabel = "?";
			}
		}
		
		private void resolveImage(){
			EntryType et = item.prescription.getEntryType();
			switch (et) {
			case FIXED_MEDICATION:
				item.image = Images.IMG_FIX_MEDI.getImage();
				break;
			case RESERVE_MEDICATION:
				item.image = Images.IMG_RESERVE_MEDI.getImage();
				break;
			case SYMPTOMATIC_MEDICATION:
				item.image = Images.IMG_SYMPTOM_MEDI.getImage();
				break;
			case SELF_DISPENSED:
				if (item.prescription.isApplied()) {
					item.image = Images.IMG_SYRINGE.getImage();
					break;
				}
				item.image = Images.IMG_VIEW_CONSULTATION_DETAIL.getImage();
				break;
			case RECIPE:
				item.image = Images.IMG_VIEW_RECIPES.getImage();
				break;
			default:
				item.image = Images.IMG_EMPTY_TRANSPARENT.getImage();
				break;
			}
		}
		
		private void resolveLastDisposed(){
			IPersistentObject ld = item.prescription.getLastDisposed(item.rezeptId);
			if (ld == null) {
				item.lastDisposed = StringConstants.EMPTY;
			} else {
				item.lastDisposed = ld;
			}
		}
		
		private void resolveStopReason(){
			String reason = item.prescription.getStopReason();
			if (reason != null) {
				item.stopReason = reason;
			} else {
				item.stopReason = "";
			}
		}
		
		private void resolvePrescriptorLabel(){
			if (item.prescriptorId != null && !item.prescriptorId.isEmpty()) {
				Anwender prescriptor = Anwender.load(item.prescriptorId);
				if (prescriptor != null && prescriptor.exists()) {
					item.prescriptorLabel = prescriptor.getKuerzel();
					if (item.prescriptorLabel == null || item.prescriptorLabel.isEmpty()) {
						Query<User> query = new Query<>(User.class);
						query.add(User.FLD_ASSOC_CONTACT, Query.EQUALS, item.prescriptorId);
						List<User> users = query.execute();
						if (!users.isEmpty()) {
							item.prescriptorLabel = users.get(0).getId();
						}
					}
					return;
				}
			}
			item.prescriptorLabel = "";
		}
		
		private void resolveDisposalComment(){
			String comment = item.prescription.getDisposalComment();
			if (comment != null) {
				item.disposalComment = comment;
			} else {
				item.disposalComment = "";
			}
		}
	}
}
