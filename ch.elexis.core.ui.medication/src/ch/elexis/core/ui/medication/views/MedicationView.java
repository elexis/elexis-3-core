package ch.elexis.core.ui.medication.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.ArticleDefaultSignatureTitleAreaDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.medication.PreferenceConstants;
import ch.elexis.core.ui.medication.action.MovePrescriptionPositionInTableDownAction;
import ch.elexis.core.ui.medication.action.MovePrescriptionPositionInTableUpAction;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;

public class MedicationView extends ViewPart implements IActivationListener {
	public MedicationView(){}
	
	private MedicationComposite tpc;
	private TableViewer medicationTableViewer;
	private Patient currentPat;
	
	public static final String PART_ID = "ch.elexis.core.ui.medication.views.MedicationView"; //$NON-NLS-1$
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev){
			Patient sp = ElexisEventDispatcher.getSelectedPatient();
			if (sp == null) {
				updateUi(sp);
				return;
			}
			
			if (sp.equals(currentPat)) {
				return;
			}
			
			currentPat = sp;
			updateUi(sp);
		}
	};
	
	private ElexisEventListener eeli_presc = new ElexisUiEventListenerImpl(Prescription.class,
		ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_UPDATE) {
		public void runInUi(ElexisEvent ev){
			updateUi(ElexisEventDispatcher.getSelectedPatient());
		}
	};
	
	@Override
	public void createPartControl(Composite parent){
		tpc = new MedicationComposite(parent, SWT.NONE);
		
		// register context menu for table viewer
		medicationTableViewer = tpc.getMedicationTableViewer();
		MenuManager menuManager = new MenuManager();
		menuManager.add(new MovePrescriptionPositionInTableUpAction(medicationTableViewer, tpc));
		menuManager.add(new MovePrescriptionPositionInTableDownAction(medicationTableViewer, tpc));
		menuManager.add(new Separator());
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator());
		menuManager.add(new Action() {
			{
				setImageDescriptor(Images.IMG_BOOKMARK_PENCIL.getImageDescriptor());
				setText(Messages.FixMediDisplay_AddDefaultSignature);
			}
			
			@Override
			public void run(){
				StructuredSelection ss = (StructuredSelection) medicationTableViewer.getSelection();
				Prescription pr = (Prescription) ss.getFirstElement();
				if (pr != null) {
					ArticleDefaultSignatureTitleAreaDialog adtad =
						new ArticleDefaultSignatureTitleAreaDialog(UiDesk.getTopShell(), pr);
					adtad.open();
				}
			}
		});
		Menu menu = menuManager.createContextMenu(medicationTableViewer.getTable());
		
		medicationTableViewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuManager, medicationTableViewer);
		getSite().setSelectionProvider(medicationTableViewer);
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	public void setMedicationTableViewerComparator(ViewerSortOrder order){
		medicationTableViewer.setComparator(order.vc);
		CoreHub.userCfg.set(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ORDER, order.val);
	}
	
	@Override
	public void setFocus(){
		if (currentPat == null) {
			Patient sp = ElexisEventDispatcher.getSelectedPatient();
			updateUi(sp);
			currentPat = sp;
		}
	}
	
	private void updateUi(Patient pat){
		if (pat == null) {
			tpc.updateUi(null);
			return;
		}
		
		Query<Prescription> qbe = new Query<Prescription>(Prescription.class,
			Prescription.FLD_PATIENT_ID, pat.getId(), Prescription.TABLENAME, new String[] {
				Prescription.FLD_DOSAGE, Prescription.FLD_DATE_FROM, Prescription.FLD_DATE_UNTIL,
				Prescription.FLD_REZEPT_ID, Prescription.FLD_PRESC_TYPE, Prescription.FLD_REMARK
		});
		List<Prescription> result = qbe.execute();
		tpc.updateUi(result);
	}
	
	@Override
	public void activation(boolean mode){
		if (mode) {
			setFocus();
			int sorter = CoreHub.userCfg.get(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ORDER, 1);
			medicationTableViewer.setComparator(ViewerSortOrder.getSortOrderPerValue(sorter).vc);
		}
	}
	
	@Override
	public void visible(boolean mode){
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_presc);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_presc);
		}
		
	}
	
	public void refresh(){
		medicationTableViewer.refresh();
	}
	
	public void resetSelection(){
		tpc.resetSelectedMedication();
	}
}
