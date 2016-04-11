package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.rgw.tools.StringTool;

public class SwitchMedicationHandler extends AbstractHandler {
	private static final Logger log = LoggerFactory.getLogger(SwitchMedicationHandler.class);
	
	private static final String UPCASES_DASH_NR_PATTERN = "[A-Z-0-9]+";
	private static final String SPACE = "\\s";
	private static final String NUMBERS = "[0-9]+";
	
	private static PersistentObjectDropTarget dropTarget;
	private static MedicationView medicationView;
	private LeistungenView leistungenView;
	private Prescription originalPresc;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			
			if (firstElement instanceof MedicationTableViewerItem) {
				MedicationTableViewerItem mtvItem = (MedicationTableViewerItem) firstElement;
				Prescription p = mtvItem.getPrescription();
				if (p != null) {
					originalPresc = p;
					copyShortArticleNameToClipboard();
					openLeistungsView();
				}
			}
		}
		return null;
	}
	
	/**
	 * Uses first parts of an article name and copies it to the clipboard. <br>
	 * Usually including everything from the start that is UPPER-CASE (including UPPER-CASE with
	 * DASH or NUMBER mixtures). In case article name does not fulfill common structure at least the
	 * first 2 parts (words) are included.
	 * 
	 */
	private void copyShortArticleNameToClipboard(){
		Artikel article = originalPresc.getArtikel();
		if (article == null)
			return;
			
		String fullName = article.getName();
		String[] nameParts = fullName.split(SPACE);
		StringBuilder sbFilterName = new StringBuilder();
		
		for (int i = 0; i < nameParts.length; i++) {
			String s = nameParts[i];
			if (i > 0) {
				sbFilterName.append(" ");
			}
			
			// matches upper cases, dash an probably number but not only numbers
			if (s.matches(UPCASES_DASH_NR_PATTERN) && !s.matches(NUMBERS)) {
				sbFilterName.append(s);
			} else {
				// at least first two parts of name are included
				if (i < 2) {
					sbFilterName.append(s);
				} else {
					break;
				}
			}
		}
		
		// copy text to clipboard
		Clipboard cb = new Clipboard(UiDesk.getDisplay());
		TextTransfer textTransfer = TextTransfer.getInstance();
		cb.setContents(new String[] {
			sbFilterName.toString()
		}, new Transfer[] {
			textTransfer
		});
	}
	
	private void openLeistungsView(){
		medicationView = (MedicationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(MedicationView.PART_ID);
			
		if (dropTarget == null) {
			dropTarget = new PersistentObjectDropTarget("FixMedication", UiDesk.getTopShell(),
				new DropFixMedicationReceiver());
		}
		
		// open the LeistungenView
		try {
			if (StringTool.isNothing(LeistungenView.ID)) {
				log.debug("LeistungenView.ID empty or not found");
				SWTHelper.alert("Fehler", "LeistungenView.ID");
			}
			
			medicationView.getViewSite().getPage().showView(LeistungenView.ID);
			leistungenView = (LeistungenView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(LeistungenView.ID);
			CodeSelectorHandler csHandler = CodeSelectorHandler.getInstance();
			csHandler.setCodeSelectorTarget(dropTarget);
			csHandler.getCodeSelectorTarget().registered(false);
			
			for (CTabItem cti : leistungenView.ctab.getItems()) {
				if (cti.getText().equalsIgnoreCase("Artikelstamm")) {
					leistungenView.setSelected(cti);
					leistungenView.setFocus();
					leistungenView.ctab.setSelection(cti);
				}
			}
		} catch (Exception e) {
			log.error(
				"Could not open LeistungenView from the MedicationView in order to switch medication",
				e);
		}
	}
	
	/**
	 * waits for dropps/double-clicks on a medication
	 *
	 */
	private final class DropFixMedicationReceiver implements PersistentObjectDropTarget.IReceiver {
		public void dropped(PersistentObject article, DropTargetEvent ev){
			
			String dosage = originalPresc.getDosis();
			String remark = originalPresc.getBemerkung();
			if (dosage == null || dosage.isEmpty()) {
				ArticleDefaultSignature defSig =
					ArticleDefaultSignature.getDefaultsignatureForArticle((Artikel) article);
				if (defSig != null) {
					dosage = defSig.getSignatureAsDosisString();
					remark = defSig.getSignatureComment();
				}
			}
			
			Prescription presc = new Prescription(originalPresc);
			presc.set(Prescription.FLD_ARTICLE, article.storeToString());
			presc.setDosis(dosage);
			presc.setBemerkung(remark);
			presc.setPrescType(EntryType.FIXED_MEDICATION.getFlag(), true);
			
			// stop prev medication
			originalPresc.stop(null);
			originalPresc.setStopReason("Ersetzt durch " + ((Artikel) article).getName());
			
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
