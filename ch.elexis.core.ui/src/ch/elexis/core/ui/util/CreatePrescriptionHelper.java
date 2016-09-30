package ch.elexis.core.ui.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.dialogs.PrescriptionSignatureTitleAreaDialog;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.ArticleDefaultSignature.ArticleSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class CreatePrescriptionHelper {
	
	private Artikel article;
	private Shell parentShell;
	
	public CreatePrescriptionHelper(Artikel article, Shell parentShell){
		this.article = article;
		this.parentShell = parentShell;
	}
	
	public void createPrescription(){
		ArticleDefaultSignature defaultSignature =
			ArticleDefaultSignature.getDefaultsignatureForArticle((Artikel) article);
		
		ArticleSignature signature;
		if (defaultSignature != null) {
			signature = ArticleSignature.fromDefault(defaultSignature);
		} else {
			PrescriptionSignatureTitleAreaDialog dialog =
				new PrescriptionSignatureTitleAreaDialog(parentShell, (Artikel) article);
			if (dialog.open() != Dialog.OK) {
				return;
			}
			signature = dialog.getSignature();
		}
		createPrescriptionFromSignature(signature);
	}
	
	public void createPrescriptionFromSignature(ArticleSignature signature){
		// create medication entry
		Prescription prescription = new Prescription((Artikel) article,
			(Patient) ElexisEventDispatcher.getSelected(Patient.class),
			signature.getSignatureAsDosisString(), signature.getComment());
		AcquireLockUi.aquireAndRun(prescription, new ILockHandler() {
			@Override
			public void lockFailed(){
				prescription.remove();
			}
			
			@Override
			public void lockAcquired(){
				prescription.setEntryType(signature.getMedicationType());
			}
		});
		// create dispensation entry
		if (signature.getDisposalType() != EntryType.RECIPE) {
			EntryType disposalType = signature.getDisposalType();
			if (disposalType == EntryType.SELF_DISPENSED) {
				selfDispense(prescription);
			}
		}
	}
	
	public void selfDispense(Prescription prescription){
		// add article to consultation
		Konsultation kons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		if (kons != null) {
			boolean isToday = new TimeTool(kons.getDatum()).isSameDay(new TimeTool());
			if (isToday) {
				Artikel dispensationArticle = prescription.getArtikel();
				Result<IVerrechenbar> result = kons.addLeistung(dispensationArticle);
				if (result.isOK()) {
					// work is done
					return;
				}
			}
		}
		MessageDialog.openInformation(parentShell, "Konsultation ungültig",
			"Keine Konsultation selektiert, oder die selektierte Konsultation ist nicht von heute. Dispensation wurde abgebrochen.");
	}
}
