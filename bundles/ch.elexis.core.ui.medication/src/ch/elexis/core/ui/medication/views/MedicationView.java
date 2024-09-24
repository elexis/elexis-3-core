package ch.elexis.core.ui.medication.views;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.medication.PreferenceConstants;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;
import ch.elexis.core.ui.views.IRefreshable;

public class MedicationView extends ViewPart implements IRefreshable {
	public MedicationView() {
	}

	private MedicationComposite tpc;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this) {
		public void partActivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
			super.partActivated(partRef);
			if (tpc != null && !tpc.isDisposed()) {
				tpc.showMedicationDetailComposite(null);
			}
		};
	};

	public static final String PART_ID = "ch.elexis.core.ui.medication.views.MedicationView"; //$NON-NLS-1$

	@Inject
	void activePatient(@Optional IPatient patient) {
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(tpc)) {
				updateUi(patient, false);
			}
		});
	}

	@Optional
	@Inject
	void udpatePatient(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IPatient patient) {
		if (CoreUiUtil.isActiveControl(tpc)) {
			// only update with info of selected patient
			if (patient != null && patient.equals(ContextServiceHolder.get().getActivePatient().orElse(null))) {
				updateUi(patient, false);				
			}
		}
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IPrescription.class.equals(clazz)) {
			if (CoreUiUtil.isActiveControl(tpc)) {
				Display.getDefault().asyncExec(() -> {
					refresh();
				});
			}
		}
	}

	@Inject
	@Optional
	public void processBilledArticleEvent(
			@UIEventTopic(ElexisEventTopics.EVENT_ARTICLE_PROCESSED) Map<String, Object> eventPayload) {
		if (eventPayload != null) {
			IArticle article = (IArticle) eventPayload.get(ExtensionPointConstantsUi.PAYLOAD_ARTICLE);
			IArticleDefaultSignature signature = (IArticleDefaultSignature) eventPayload
					.get(ExtensionPointConstantsUi.PAYLOAD_SIGNATURE);
			IBilled billed = (IBilled) eventPayload.get(ExtensionPointConstantsUi.PAYLOAD_BILLED);
			if (article != null && signature != null) {
				CreatePrescriptionHelper prescriptionHelper = new CreatePrescriptionHelper(article,
						getSite().getShell());
				IPrescription prescription = prescriptionHelper.createPrescriptionFromSignature(signature);
				if (prescription != null && billed != null) {
					prescription.setExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID,
							billed.getId().toString());
					CoreModelServiceHolder.get().save(prescription);
				}
				Display.getDefault().asyncExec(() -> {
					ContextServiceHolder.get().getActivePatient().ifPresent(patient -> {
						updateUi(patient, true);
					});
				});
			}
		}
	}

	@Optional
	@Inject
	void updatePrescription(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IPrescription prescription) {
		if (CoreUiUtil.isActiveControl(tpc)) {
			if (prescription != null) {
				if (!getMedicationComposite().isShowingHistory()) {
					EntryType entryType = prescription.getEntryType();
					if (entryType == EntryType.RECIPE || entryType == EntryType.SELF_DISPENSED) {
						return;
					}
				}
				// only update with info of selected patient
				if (prescription.getPatient().equals(ContextServiceHolder.get().getActivePatient().orElse(null))) {
					updateUi(prescription.getPatient(), true);
				}
			}
		}
	}

	@Inject
	void createPrescription(@Optional @UIEventTopic(ElexisEventTopics.EVENT_CREATE) IPrescription prescription) {
		updatePrescription(prescription);
	}

	@Inject
	void reloadPrescription(@Optional @UIEventTopic(ElexisEventTopics.EVENT_CREATE) Class<?> clazz) {
		if (clazz == IPrescription.class) {
			if (CoreUiUtil.isActiveControl(tpc)) {
				ContextServiceHolder.get().getActivePatient().ifPresent(patient -> {
					updateUi(patient, false);
				});
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		tpc = new MedicationComposite(parent, SWT.NONE, getSite());
		getSite().setSelectionProvider(tpc);
		int sorter = ConfigServiceHolder.getUser(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ORDER, 1);
		tpc.setViewerSortOrder(ViewerSortOrder.getSortOrderPerValue(sorter));

		ViewerSortOrder.getSortOrderPerValue(sorter)
				.setAtcSort(ConfigServiceHolder.getUser(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ATC, false));
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	public void setMedicationTableViewerComparator(ViewerSortOrder order) {
		tpc.setViewerSortOrder(order);
		ConfigServiceHolder.setUser(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ORDER, order.val);
	}

	public ViewerSortOrder getMedicationTableViewerComparator() {
		return tpc.getViewerSortOrder();
	}

	@Override
	public void setFocus() {
		tpc.setFocus();
		refresh();
	}

	private void updateUi(IPatient patient, boolean forceUpdate) {
		LoggerFactory.getLogger(getClass()).info("[MEDI PAT] " + (patient != null ? patient.getId() : "null"));
		tpc.updateUi(patient, forceUpdate);
	}

	public void refresh() {
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(tpc)) {
				updateUi(ContextServiceHolder.get().getActivePatient().orElse(null), false);
			}
		});
	}

	public void resetSelection() {
		tpc.resetSelectedMedication();
	}

	public MedicationComposite getMedicationComposite() {
		return tpc;
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
