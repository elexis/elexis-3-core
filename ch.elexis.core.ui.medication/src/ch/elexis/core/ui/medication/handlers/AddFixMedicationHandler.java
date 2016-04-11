package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.rgw.tools.StringTool;

public class AddFixMedicationHandler extends AbstractHandler {
	private static PersistentObjectDropTarget dropTarget;
	private static MedicationView medicationView;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		medicationView =
			(MedicationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(MedicationView.PART_ID);
		
		if (dropTarget == null) {
			dropTarget =
				new PersistentObjectDropTarget("FixMedication", UiDesk.getTopShell(),
					new DropFixMedicationReceiver());
		}
		
		// open the LeistungenView
		try {
			if (StringTool.isNothing(LeistungenView.ID)) {
				SWTHelper.alert("Fehler", "LeistungenView.ID");
			}
			
			medicationView.getViewSite().getPage().showView(LeistungenView.ID);
			CodeSelectorHandler csHandler = CodeSelectorHandler.getInstance();
			csHandler.setCodeSelectorTarget(dropTarget);
			csHandler.getCodeSelectorTarget().registered(false);
		} catch (Exception e) {
			//TODO log "Error trying to open LeistungenView
		}
		return null;
	}
	
	/**
	 * waits for dropps/double-clicks on a medication
	 *
	 */
	private final class DropFixMedicationReceiver implements PersistentObjectDropTarget.IReceiver {
		public void dropped(PersistentObject article, DropTargetEvent ev){
			ArticleDefaultSignature defSig =
				ArticleDefaultSignature.getDefaultsignatureForArticle((Artikel) article);
			
			String dosage = StringTool.leer;
			String remark = StringTool.leer;
			if (defSig != null) {
				dosage = defSig.getSignatureAsDosisString();
				remark = defSig.getSignatureComment();
			}
			
			Prescription presc = new Prescription((Artikel) article,
				(Patient) ElexisEventDispatcher.getSelected(Patient.class), dosage, remark);
			AcquireLockUi.aquireAndRun(presc, new ILockHandler() {
				
				@Override
				public void lockFailed(){
					presc.remove();
				}
				
				@Override
				public void lockAcquired(){
					presc.setPrescType(EntryType.FIXED_MEDICATION.getFlag(), true);
				}
			});
			medicationView.refresh();
		}
		
		public boolean accept(PersistentObject o){
			if (!(o instanceof Artikel))
				return false;
			// we do not accept vaccination articles
			Artikel a = (Artikel) o;
			return (!a.getATC_code().startsWith("J07"));
		}
	}
}
