package ch.elexis.core.ui.util;

import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.ui.dialogs.PrescriptionSignatureTitleAreaDialog;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.ArticleDefaultSignature.ArticleSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class CreatePrescriptionHelper {
	
	public static final String MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG =
		"medication/settings/alwaysShowSignatureDialog";
	
	public static final String MEDICATION_SETTINGS_SIGNATURE_STD_DISPENSATION =
		"medication/settings/signatureStdDispensation";
	
	private Artikel article;
	private Shell parentShell;
	
	public CreatePrescriptionHelper(Artikel article, Shell parentShell){
		this.article = article;
		this.parentShell = parentShell;
	}
	
	public void createPrescription(){
		ArticleDefaultSignature defaultSignature =
			ArticleDefaultSignature.getDefaultsignatureForArticle((Artikel) article);
		
		Optional<ArticleSignature> signature;
		if (defaultSignature != null) {
			signature = Optional.of(ArticleSignature.fromDefault(defaultSignature));
			if (CoreHub.userCfg.get(MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG, false)) {
				signature = getSignatureWithDialog(signature);
			}
		} else {
			signature = getSignatureWithDialog(Optional.empty());
		}
		signature.ifPresent(s -> createPrescriptionFromSignature(s));
	}
	
	private Optional<ArticleSignature> getSignatureWithDialog(
		Optional<ArticleSignature> preSelectedSignature){
		PrescriptionSignatureTitleAreaDialog dialog =
			new PrescriptionSignatureTitleAreaDialog(parentShell, (Artikel) article);
		preSelectedSignature.ifPresent(s -> dialog.setSignature(s));
		if (dialog.open() != Dialog.OK) {
			return Optional.empty();
		}
		return Optional.of(dialog.getSignature());
	}
	
	public void createPrescriptionFromSignature(ArticleSignature signature){
		// create medication entry
		Prescription prescription = new Prescription((Artikel) article,
			(Patient) ElexisEventDispatcher.getSelected(Patient.class),
			signature.getSignatureAsDosisString(), signature.getComment());
		prescription.setEntryType(signature.getMedicationType());
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
