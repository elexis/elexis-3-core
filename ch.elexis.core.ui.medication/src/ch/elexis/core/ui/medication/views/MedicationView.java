package ch.elexis.core.ui.medication.views;

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

public class MedicationView extends ViewPart implements IActivationListener {
	public MedicationView(){}
	
	private MedicationComposite tpc;
	private TableViewer medicationTableViewer;
	
	public static final String PART_ID = "ch.elexis.core.ui.medication.views.MedicationView"; //$NON-NLS-1$
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev){
			updateUi(ElexisEventDispatcher.getSelectedPatient(), false);
		}
	};
	
	private ElexisEventListener eeli_presc = new ElexisUiEventListenerImpl(Prescription.class,
		ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_UPDATE) {
		public void runInUi(ElexisEvent ev){
			updateUi(ElexisEventDispatcher.getSelectedPatient(), true);
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
		updateUi(ElexisEventDispatcher.getSelectedPatient(), true);
	}
	
	private void updateUi(Patient pat, boolean forceUpdate){
		tpc.updateUi(pat, forceUpdate);
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
