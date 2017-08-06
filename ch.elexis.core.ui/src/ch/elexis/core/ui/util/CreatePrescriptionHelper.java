package ch.elexis.core.ui.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.model.eigenartikel.Constants;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.dialogs.PrescriptionSignatureTitleAreaDialog;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.ArticleDefaultSignature.ArticleSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.data.Prescription;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class CreatePrescriptionHelper {
	
	public static final String MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG =
		"medication/settings/alwaysShowSignatureDialog";
	
	public static final String MEDICATION_SETTINGS_SIGNATURE_STD_DISPENSATION =
		"medication/settings/signatureStdDispensation";
	
	public static final String MEDICATION_SETTINGS_DISPENSE_ARTIKELSTAMM_CONVERT =
		"medication/settings/artikelstammConvert";
	
	private Artikel article;
	private Shell parentShell;
	
	private boolean medicationTypeFix = false;
	
	public CreatePrescriptionHelper(Artikel article, Shell parentShell){
		this.article = article;
		this.parentShell = parentShell;
	}
	
	public void setMedicationTypeFix(boolean value){
		this.medicationTypeFix = value;
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
		dialog.setMedicationTypeFix(medicationTypeFix);
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
		AcquireLockUi.aquireAndRun(prescription, new ILockHandler() {
			@Override
			public void lockFailed(){
				prescription.remove();
			}
			
			@Override
			public void lockAcquired(){
				prescription.setEntryType(signature.getMedicationType());
				// a new symptomatic medication must be created with a stop date
				if (EntryType.SYMPTOMATIC_MEDICATION.equals(signature.getMedicationType())
					&& signature.getEndDate() != null) {
					prescription.stop(signature.getEndDate());
					prescription.setStopReason("Stop geplant");
				}
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
				if (shouldUpdateToArtikelstamm() && isArtikelstammAvailable()
					&& !isEigenartikel(dispensationArticle)
					&& !isArtikelstamm(dispensationArticle)) {
					Optional<Artikel> item = getArtikelstammItem(dispensationArticle);
					if (item.isPresent()) {
						prescription.set(Prescription.FLD_ARTICLE, item.get().storeToString());
						MessageDialog.openInformation(parentShell,
							Messages.CreatePrescriptionHelper_InfoDispensationArtikelstammTitel,
							MessageFormat.format(
								Messages.CreatePrescriptionHelper_InfoDispensationArtikelstammUpate,
								dispensationArticle.getLabel(), item.get().getLabel()));
						dispensationArticle = item.get();
					} else {
						MessageDialog.openError(parentShell,
							Messages.CreatePrescriptionHelper_InfoDispensationArtikelstammTitel,
							MessageFormat.format(
								Messages.CreatePrescriptionHelper_ErrorDispensationArtikelstammUpate,
								dispensationArticle.getLabel()));
						dispensationArticle = item.get();
					}
				}
				Result<IVerrechenbar> result = kons.addLeistung(dispensationArticle);
				if (result.isOK()) {
					// work is done
					return;
				}
			}
		}
		MessageDialog.openWarning(parentShell,
			Messages.CreatePrescriptionHelper_WarninigNoConsTitle,
			Messages.CreatePrescriptionHelper_WarninigNoConsText);
	}
	
	private boolean shouldUpdateToArtikelstamm(){
		return CoreHub.userCfg.get(MEDICATION_SETTINGS_DISPENSE_ARTIKELSTAMM_CONVERT, false);
	}
	
	private boolean isEigenartikel(Artikel dispensationArticle){
		return dispensationArticle.getCodeSystemName().equals(Constants.TYPE_NAME);
	}
	
	private boolean isArtikelstamm(Artikel dispensationArticle){
		return dispensationArticle.storeToString()
			.startsWith("ch.artikelstamm.elexis.common.ArtikelstammItem");
	}
	
	private boolean isArtikelstammAvailable(){
		return PersistentObject.tableExists("ARTIKELSTAMM_CH");
	}
	
	private Optional<Artikel> getArtikelstammItem(Artikel dispensationArticle){
		String gtin = dispensationArticle.getGTIN();
		Optional<Artikel> ret = Optional.empty();
		if (gtin != null && !gtin.isEmpty()) {
			ret = loadArtikelWithGtin(gtin);
			if (ret.isPresent()) {
				return ret;
			}
		}
		String pharma = dispensationArticle.getPharmaCode();
		if (pharma != null && !pharma.isEmpty()) {
			ret = loadArtikelWithPharma(pharma);
			if (ret.isPresent()) {
				return ret;
			}
		}
		return ret;
	}
	
	private Optional<Artikel> loadArtikelWithPharma(String pharma){
		Optional<Artikel> ret = Optional.empty();
		PreparedStatement pstm = PersistentObject.getDefaultConnection()
			.getPreparedStatement("SELECT ID FROM ARTIKELSTAMM_CH WHERE PHAR=?");
		try {
			pstm.setString(1, pharma);
			ret = loadArtikel(pstm.executeQuery());
		} catch (SQLException e) {
			LoggerFactory.getLogger(CreatePrescriptionHelper.class)
				.error("Could not lookup artikelstamm with GTIN", e);
		} finally {
			PersistentObject.getDefaultConnection().releasePreparedStatement(pstm);
		}
		return ret;
	}
	
	private Optional<Artikel> loadArtikelWithGtin(String gtin){
		Optional<Artikel> ret = Optional.empty();
		PreparedStatement pstm = PersistentObject.getDefaultConnection()
			.getPreparedStatement("SELECT ID FROM ARTIKELSTAMM_CH WHERE GTIN=?");
		try {
			pstm.setString(1, gtin);
			ret = loadArtikel(pstm.executeQuery());
		} catch (SQLException e) {
			LoggerFactory.getLogger(CreatePrescriptionHelper.class)
				.error("Could not lookup artikelstamm with GTIN", e);
		} finally {
			PersistentObject.getDefaultConnection().releasePreparedStatement(pstm);
		}
		return ret;
	}
	
	private Optional<Artikel> loadArtikel(ResultSet result) throws SQLException{
		Optional<Artikel> ret = Optional.empty();
		while (result.next()) {
			ret = loadArtikelstamm(result.getString(1));
			if (ret.isPresent()) {
				break;
			}
		}
		result.close();
		return ret;
	}
	
	private Optional<Artikel> loadArtikelstamm(String id){
		PersistentObjectFactory factory = new PersistentObjectFactory();
		PersistentObject item = factory.createFromString(
			"ch.artikelstamm.elexis.common.ArtikelstammItem" + StringConstants.DOUBLECOLON + id);
		if (item != null && item.exists()) {
			return Optional.of((Artikel) item);
		}
		return Optional.empty();
	}
}
