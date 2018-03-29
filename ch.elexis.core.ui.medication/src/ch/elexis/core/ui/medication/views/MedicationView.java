package ch.elexis.core.ui.medication.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.medication.PreferenceConstants;
import ch.elexis.core.ui.views.IRefreshable;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;

public class MedicationView extends ViewPart implements IRefreshable {
	public MedicationView(){}
	
	private MedicationComposite tpc;
	
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);
	
	public static final String PART_ID = "ch.elexis.core.ui.medication.views.MedicationView"; //$NON-NLS-1$
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev){
			if (isActiveControl(tpc)) {
				updateUi((Patient) ev.getObject(), false);
			}
		}
	};
	
	private ElexisEventListener eeli_presc = new ElexisUiEventListenerImpl(Prescription.class,
		ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_UPDATE) {
		public void runInUi(ElexisEvent ev){
			if (isActiveControl(tpc)) {
				PersistentObject prescObj = ev.getObject();
				if (prescObj instanceof Prescription) {
					// ignore updates of recipe and self dispensed entries, if not showing history
					if (!getMedicationComposite().isShowingHistory()) {
						EntryType entryType = ((Prescription) prescObj).getEntryType();
						if (entryType == EntryType.RECIPE
							|| entryType == EntryType.SELF_DISPENSED) {
							return;
						}
						
					}
				}
				updateUi(ElexisEventDispatcher.getSelectedPatient(), true);
			}
		}
	};
	
	@Override
	public void createPartControl(Composite parent){
		tpc = new MedicationComposite(parent, SWT.NONE, getSite());
		getSite().setSelectionProvider(tpc);
		int sorter = CoreHub.userCfg.get(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ORDER, 1);
		tpc.setViewerSortOrder(ViewerSortOrder.getSortOrderPerValue(sorter));
		
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_presc);
		getSite().getPage().addPartListener(udpateOnVisible);
	}
	
	@Override
	public void dispose(){
		getSite().getPage().removePartListener(udpateOnVisible);
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_presc);
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
	
	private void updateUi(Patient pat, boolean forceUpdate){
		tpc.updateUi(pat, forceUpdate);
	}
	
	public void refresh(){
		eeli_pat.catchElexisEvent(ElexisEvent.createPatientEvent());
	}
	
	public void resetSelection(){
		tpc.resetSelectedMedication();
	}
	
	public MedicationComposite getMedicationComposite(){
		return tpc;
	}
}
