package ch.elexis.core.ui.medication.views;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.rgw.tools.TimeTool;

/**
 * Maps an element of type {@link IPrescription} for presentation within the MedicationTableViewer.
 * Used for performance reasons.
 */
public class MedicationTableViewerItem {
	
	private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private static ExecutorService executorService = Executors.newFixedThreadPool(8);
	
	private StructuredViewer viewer;
	
	// loaded first run
	private IPrescription prescription;
	private IArticle article;
	private LocalDateTime dateFrom;
	private LocalDateTime dateUntil;
	private String dosis;
	private String remark;
	private String disposalComment;
	private int sortOrder;
	private IContact prescriptor;
	
	// lazy computed
	private String artikelLabel;
	private Identifiable lastDisposed;
	private String prescriptorLabel;
	private String stopReason;
	private Image image;
	
	private Date endTime;
	
	private boolean resolved = false;
	private boolean resolving = false;
	
	private MedicationTableViewerItem(IPrescription prescription, StructuredViewer viewer){
		this.viewer = viewer;
		this.prescription = prescription;
		article = prescription.getArticle();
		dosis = prescription.getDosageInstruction();
		remark = prescription.getDisposalComment();
		sortOrder = prescription.getSortOrder();
		prescriptor = prescription.getPrescriptor();
		
		dateFrom = prescription.getDateFrom();
		dateUntil = prescription.getDateTo();
		
		if (dateUntil != null) {
			endTime = Date.from(dateUntil.atZone(ZoneId.systemDefault()).toInstant());
		} else {
			endTime = new TimeTool().getTime();
		}
	}
	
	public static List<MedicationTableViewerItem> createFromPrescriptionList(
		List<IPrescription> prescriptionList, StructuredViewer viewer){
		List<MedicationTableViewerItem> collect = prescriptionList.stream()
			.map(p -> new MedicationTableViewerItem(p, viewer)).collect(Collectors.toList());
		return collect;
	}
	
	// loaded with first run
	public String getId(){
		return prescription.getId();
	}
	
	public String getBeginDate(){
		return dateFrom != null ? dateFormatter.format(dateFrom) : "";
	}
	
	public String getEndDate(){
		return dateUntil != null ? dateFormatter.format(dateUntil) : "";
	}
	
	public Date getEndTime(){
		return endTime;
	}
	
	public void setEndTime(Date time){
		endTime = time;
	}
	
	public String getDosis(){
		return dosis != null ? dosis : "";
	}
	
	public IPrescription getPrescription(){
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
	
	/**
	 * An active medication is not a recipe and not a dispensation and is not stopped.
	 * 
	 * @return
	 */
	public boolean isActiveMedication(){
		EntryType entryType = prescription.getEntryType();
		return entryType != EntryType.RECIPE && entryType != EntryType.SELF_DISPENSED
			&& !isStopped();
	}
	
	public Identifiable getLastDisposed(){
		if (lastDisposed == null) {
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
		}
		return lastDisposed;
	}
	
	public EntryType getEntryType(){
		return prescription.getEntryType();
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
	
	public void setOrder(int i){
		sortOrder = i;
		prescription.setSortOrder(i);
		CoreModelServiceHolder.get().save(prescription);
	}
	
	public int getOrder(){
		return sortOrder;
	}
	
	public boolean isStopped(){
		return dateUntil != null;
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
			if (item.article != null) {
				item.artikelLabel = item.article.getLabel();
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
			IRecipe recipe = item.prescription.getRecipe();
			IBilled billed = item.prescription.getBilled();
			if (recipe != null) {
				item.lastDisposed = recipe;
			} else if (billed != null && billed.getEncounter() != null) {
				item.lastDisposed = billed;
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
			item.prescriptorLabel = "";
			if (item.prescriptor != null) {
				item.prescriptorLabel = item.prescriptor.getLabel();
			}
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
	
	public IRecipe getRecipe(){
		return prescription.getRecipe();
	}
	
	public IBilled getBilled(){
		return prescription.getBilled();
	}
}
