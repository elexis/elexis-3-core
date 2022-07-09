package ch.elexis.core.ui.util;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.localarticle.Constants;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.dialogs.PrescriptionSignatureTitleAreaDialog;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

public class CreatePrescriptionHelper {

	public static final String MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG = "medication/settings/alwaysShowSignatureDialog"; //$NON-NLS-1$

	public static final String MEDICATION_SETTINGS_SIGNATURE_STD_DISPENSATION = "medication/settings/signatureStdDispensation"; //$NON-NLS-1$

	public static final String MEDICATION_SETTINGS_DISPENSE_ARTIKELSTAMM_CONVERT = "medication/settings/artikelstammConvert"; //$NON-NLS-1$

	private IArticle article;
	private Shell parentShell;

	private boolean medicationTypeFix = false;

	public CreatePrescriptionHelper(IArticle article, Shell parentShell) {
		this.article = article;
		this.parentShell = parentShell;
	}

	public void setMedicationTypeFix(boolean value) {
		this.medicationTypeFix = value;
	}

	public void createPrescription() {
		Optional<IArticleDefaultSignature> defaultSignature = MedicationServiceHolder.get()
				.getDefaultSignature(article);

		Optional<IArticleDefaultSignature> signature = Optional.empty();
		if (defaultSignature.isPresent()) {
			signature = defaultSignature;
			if (ConfigServiceHolder.getUser(MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG, false)) {
				signature = getSignatureWithDialog(defaultSignature);
			}
		} else {
			signature = getSignatureWithDialog(Optional.empty());
		}
		signature.ifPresent(s -> createPrescriptionFromSignature(s));
	}

	private Optional<IArticleDefaultSignature> getSignatureWithDialog(
			Optional<IArticleDefaultSignature> preSelectedSignature) {
		PrescriptionSignatureTitleAreaDialog dialog = new PrescriptionSignatureTitleAreaDialog(parentShell, article);
		dialog.setMedicationTypeFix(medicationTypeFix);
		preSelectedSignature.ifPresent(s -> dialog.setSignature(s));
		if (dialog.open() != Dialog.OK) {
			return Optional.empty();
		}
		return Optional.of(dialog.getSignature());
	}

	public void createPrescriptionFromSignature(IArticleDefaultSignature signature) {
		// create medication entry
		IPrescription prescription = new IPrescriptionBuilder(CoreModelServiceHolder.get(), ContextServiceHolder.get(),
				article, ContextServiceHolder.get().getActivePatient().get(), signature.getSignatureAsDosisString())
						.build();
		prescription.setRemark(signature.getComment());
		prescription.setEntryType(signature.getMedicationType());
		// a new symptomatic medication can have a stop date
		if (EntryType.SYMPTOMATIC_MEDICATION.equals(signature.getMedicationType()) && signature.getEndDate() != null) {
			prescription.setDateTo(signature.getEndDate().atTime(23, 59, 59));
			prescription.setStopReason("Stop geplant");
		}
		// create dispensation entry
		if (signature.getDisposalType() != EntryType.RECIPE) {
			EntryType disposalType = signature.getDisposalType();
			if (disposalType == EntryType.SELF_DISPENSED) {
				selfDispense(prescription);
			}
		}
		if (signature.getStartDate() != null) {
			prescription.setDateFrom(signature.getStartDate().atStartOfDay());
		}
		CoreModelServiceHolder.get().save(prescription);
	}

	public void selfDispense(IPrescription prescription) {
		// add article to consultation
		Optional<IEncounter> encounter = ContextServiceHolder.get().getTyped(IEncounter.class);
		if (encounter.isPresent()) {
			boolean isToday = encounter.get().getDate().equals(LocalDate.now());
			if (isToday) {
				IArticle dispensationArticle = prescription.getArticle();
				if (shouldUpdateToArtikelstamm() && isArtikelstammAvailable() && !isEigenartikel(dispensationArticle)
						&& !isArtikelstamm(dispensationArticle)) {
					Optional<IArticle> item = getArtikelstammItem(dispensationArticle);
					if (item.isPresent()) {
						prescription.setArticle(item.get());
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
				Result<IBilled> result = BillingServiceHolder.get().bill(dispensationArticle, encounter.get(), 1);
				if (result.isOK()) {
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, encounter.get());
					// work is done
					return;
				}
			}
		}
		MessageDialog.openWarning(parentShell, Messages.CreatePrescriptionHelper_WarninigNoConsTitle,
				Messages.CreatePrescriptionHelper_WarninigNoConsText);
	}

	private boolean shouldUpdateToArtikelstamm() {
		return ConfigServiceHolder.getUser(MEDICATION_SETTINGS_DISPENSE_ARTIKELSTAMM_CONVERT, false);
	}

	private boolean isEigenartikel(IArticle dispensationArticle) {
		return dispensationArticle.getCodeSystemName().equals(Constants.TYPE_NAME);
	}

	private boolean isArtikelstamm(IArticle dispensationArticle) {
		return StoreToStringServiceHolder.getStoreToString(dispensationArticle)
				.startsWith("ch.artikelstamm.elexis.common.ArtikelstammItem"); //$NON-NLS-1$
	}

	private boolean isArtikelstammAvailable() {
		return PersistentObject.tableExists("ARTIKELSTAMM_CH"); //$NON-NLS-1$
	}

	private Optional<IArticle> getArtikelstammItem(IArticle dispensationArticle) {
		String gtin = dispensationArticle.getGtin();
		Optional<IArticle> ret = Optional.empty();
		if (gtin != null && !gtin.isEmpty()) {
			ret = loadArtikelWithCode(gtin);
		}
		if (!ret.isPresent()) {
			String possiblePharma = dispensationArticle.getCode();
			if (possiblePharma != null && !possiblePharma.isEmpty()) {
				ret = loadArtikelWithCode(possiblePharma);
			}
		}
		return ret;
	}

	private Optional<IArticle> loadArtikelWithCode(String gtin) {
		Optional<ICodeElementServiceContribution> contribution = CodeElementServiceHolder.get()
				.getContribution(CodeElementTyp.ARTICLE, "Artikelstamm"); //$NON-NLS-1$
		if (contribution.isPresent()) {
			Optional<ICodeElement> loadedArticle = contribution.get().loadFromCode(gtin, Collections.emptyMap());
			if (loadedArticle.isPresent()) {
				return Optional.of((IArticle) loadedArticle.get());
			}
		} else {
			LoggerFactory.getLogger(getClass()).warn("No Artikelstamm ICodeElementServiceContribution available"); //$NON-NLS-1$
		}
		return Optional.empty();
	}
}
