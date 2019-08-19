package ch.elexis.core.ui.medication.views;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.medication.PreferenceConstants;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.views.IRefreshable;

public class MedicationView extends ViewPart implements IRefreshable {
	public MedicationView(){}
	
	private MedicationComposite tpc;
	
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this) {
		public void partActivated(org.eclipse.ui.IWorkbenchPartReference partRef){
			super.partActivated(partRef);
			if (tpc != null && !tpc.isDisposed()) {
				tpc.showMedicationDetailComposite(null);
			}
		};
	};
	
	public static final String PART_ID = "ch.elexis.core.ui.medication.views.MedicationView"; //$NON-NLS-1$
	
	@Inject
	void activePatient(@Optional IPatient patient){
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(tpc)) {
				updateUi(patient, false);
			}
		});
	}
	
	@Optional
	@Inject
	void udpatePatient(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IPatient patient){
		if (CoreUiUtil.isActiveControl(tpc)) {
			updateUi(patient, false);
		}
	}
	
	@Optional
	@Inject
	void updatePrescription(
		@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IPrescription prescription){
		if (CoreUiUtil.isActiveControl(tpc)) {
			if (prescription != null) {
				if (!getMedicationComposite().isShowingHistory()) {
					EntryType entryType = prescription.getEntryType();
					if (entryType == EntryType.RECIPE || entryType == EntryType.SELF_DISPENSED) {
						return;
					}
				}
				updateUi(prescription.getPatient(), true);
			}
		}
	}
	
	@Inject
	void createPrescription(
		@Optional @UIEventTopic(ElexisEventTopics.EVENT_CREATE) IPrescription prescription){
		updatePrescription(prescription);
	}
	
	@Inject
	void reloadPrescription(
		@Optional @UIEventTopic(ElexisEventTopics.EVENT_CREATE) Class<?> clazz){
		if (clazz == IPrescription.class) {
			if (CoreUiUtil.isActiveControl(tpc)) {
				ContextServiceHolder.get().getActivePatient().ifPresent(patient -> {
					updateUi(patient, false);
				});
			}
		}
	}
	
	@Override
	public void createPartControl(Composite parent){
		tpc = new MedicationComposite(parent, SWT.NONE, getSite());
		getSite().setSelectionProvider(tpc);
		int sorter = CoreHub.userCfg.get(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ORDER, 1);
		tpc.setViewerSortOrder(ViewerSortOrder.getSortOrderPerValue(sorter));
		
		getSite().getPage().addPartListener(udpateOnVisible);
	}
	
	@Override
	public void dispose(){
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}
	
	public void setMedicationTableViewerComparator(ViewerSortOrder order){
		tpc.setViewerSortOrder(order);
		CoreHub.userCfg.set(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ORDER, order.val);
	}
	
	@Override
	public void setFocus(){
		tpc.setFocus();
		refresh();
	}
	
	private void updateUi(IPatient patient, boolean forceUpdate){
		tpc.updateUi(patient, forceUpdate);
	}
	
	public void refresh(){
		if (CoreUiUtil.isActiveControl(tpc)) {
			Display.getDefault().asyncExec(() -> {
				updateUi(ContextServiceHolder.get().getActivePatient().orElse(null), false);
			});
		}
	}
	
	public void resetSelection(){
		tpc.resetSelectedMedication();
	}
	
	public MedicationComposite getMedicationComposite(){
		return tpc;
	}
}
