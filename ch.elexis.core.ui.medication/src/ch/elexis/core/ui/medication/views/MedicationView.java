package ch.elexis.core.ui.medication.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.ArticleDefaultSignatureTitleAreaDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.medication.action.MovePrescriptionPositionInTableDownAction;
import ch.elexis.core.ui.medication.action.MovePrescriptionPositionInTableUpAction;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class MedicationView extends ViewPart implements IActivationListener {
	public MedicationView(){}
	
	private MedicationComposite tpc;
	private IViewSite viewSite;
	
	public static final String PART_ID = "ch.elexis.core.ui.medication.views.MedicationView"; //$NON-NLS-1$
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev){
			updateUi(ElexisEventDispatcher.getSelectedPatient());
		}
	};
	
	private ElexisEventListener eeli_presc = new ElexisUiEventListenerImpl(Prescription.class,
		ElexisEvent.EVENT_CREATE) {
		public void runInUi(ElexisEvent ev){
			updateUi(ElexisEventDispatcher.getSelectedPatient());
		}
	};
	
	@Override
	public void createPartControl(Composite parent){
		tpc = new MedicationComposite(parent, SWT.NONE);
		
		// register context menu for table viewer
		final TableViewer medicationTableViewer = tpc.getMedicationTableViewer();
		MenuManager menuManager = new MenuManager();
		menuManager.add(new MovePrescriptionPositionInTableUpAction(medicationTableViewer));
		menuManager.add(new MovePrescriptionPositionInTableDownAction(medicationTableViewer));
		menuManager.add(new Separator());
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
//		MenuManager alternativeMenuManager = new MenuManager("Alternative Medikation");
//		alternativeMenuManager.add(new AlternativMedicationContributionItem(medicationTableViewer));
//		menuManager.add(alternativeMenuManager);
		
		menuManager.add(new Separator());
		menuManager.add(new Action() {
			{
				setImageDescriptor(Images.IMG_BOOKMARK_PENCIL.getImageDescriptor());
				setText("Standard-Signatur setzen");
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
		
		// register drop target for incoming article selections
		new PersistentObjectDropTarget("", medicationTableViewer.getTable(),
			new PersistentObjectDropTarget.IReceiver() {
				
				@Override
				public void dropped(PersistentObject article, DropTargetEvent e){
					ArticleDefaultSignature defSig =
						ArticleDefaultSignature.getDefaultsignatureForArticle((Artikel) article);
					
					Prescription presc =
						new Prescription((Artikel) article, (Patient) ElexisEventDispatcher
							.getSelected(Patient.class), StringTool.leer, StringTool.leer);
					presc.set(new String[] {
						Prescription.FLD_DATE_FROM
					}, new String[] {
						new TimeTool().toString(TimeTool.DATE_GER)
					});
					if (defSig != null) {
						presc.set(Prescription.FLD_DOSAGE, defSig.getSignatureAsDosisString());
					}
					
					medicationTableViewer.refresh();
				}
				
				@Override
				public boolean accept(PersistentObject o){
					if(!(o instanceof Artikel)) return false;
					// we do not accept vaccination articles
					Artikel a = (Artikel) o;
					return (!a.getATC_code().startsWith("J07"));
				}
			}, false);
		
		viewSite = getViewSite();
		IActionBars actionBars = viewSite.getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new AddVerrechenbarAction());
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	@Override
	public void setFocus(){
		updateUi(ElexisEventDispatcher.getSelectedPatient());
	}
	
	private void updateUi(Patient pat){
		if (pat != null) {
			Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
			qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, pat.getId());
			List<Prescription> result = qbe.execute();
			tpc.updateUi(result);
		} else {
			tpc.updateUi(null);
		}
	}
	
	public class AddVerrechenbarAction extends Action {
		
		@Override
		public void run(){
			try {
				viewSite.getPage().showView(LeistungenView.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_NEW.getImageDescriptor();
		}
	}
	
	@Override
	public void activation(boolean mode){
		if (mode) {
			setFocus();
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
	
}
