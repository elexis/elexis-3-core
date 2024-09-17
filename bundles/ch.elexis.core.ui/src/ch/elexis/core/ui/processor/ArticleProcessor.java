package ch.elexis.core.ui.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.PrescriptionSignatureTitleAreaDialog;
import ch.elexis.core.ui.dialogs.ResultDialog;
import ch.rgw.tools.Result;

public class ArticleProcessor {

	private final IEncounter actEncounter;

	public ArticleProcessor(IEncounter actEncounter) {
		this.actEncounter = actEncounter;
	}

	public void processArticle(IArticle selectedArticle) {
		if (isArticleAlreadyBilled(selectedArticle)) {
			billArticleDirectly(selectedArticle);
			return;
		}
		List<IPrescription> prescriptions = getRecentPatientPrescriptions(actEncounter.getPatient());
		boolean medicationExistsType = checkIfMedicationTypeExists(prescriptions, selectedArticle);
		boolean showDialog = ConfigServiceHolder.getUser(Preferences.MEDICATION_SETTINGS_SHOW_DIALOG_ON_BILLING, false);
		if (showDialog && !medicationExistsType) {
			handleArticleBillingDialog(selectedArticle);
		} else {
			billArticleDirectly(selectedArticle);
		}
	}

	private boolean isArticleAlreadyBilled(IArticle selectedArticle) {
		return actEncounter.getBilled().stream().anyMatch(billed -> billed.getBillable() instanceof IArticle
				&& ((IArticle) billed.getBillable()).getId().equals(selectedArticle.getId()));
	}

	private void billArticleDirectly(IArticle selectedArticle) {
		Result<IBilled> billResult = BillingServiceHolder.get().bill(selectedArticle, actEncounter, 1.0);
		if (billResult.isOK()) {
			IBilled billed = billResult.get();
			CoreModelServiceHolder.get().refresh(actEncounter, true);
			postRefreshMedicationEvent();
			updatePrescriptionsWithDosage(billed);
		} else {
			ResultDialog.show(billResult);
		}
	}

	private void handleArticleBillingDialog(IArticle selectedArticle) {
	    PrescriptionSignatureTitleAreaDialog dialog = new PrescriptionSignatureTitleAreaDialog(
	            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), selectedArticle, true);
	    dialog.lookup();
	    if (dialog.open() == Dialog.OK) {
	        IArticleDefaultSignature signature = dialog.getSignature();
	        List<IPrescription> prescriptions = getRecentPatientPrescriptions(actEncounter.getPatient());
	        boolean medicationExists = checkIfMedicationExists(prescriptions, selectedArticle, signature);
	        Result<IBilled> billResult = BillingServiceHolder.get().bill(selectedArticle, actEncounter, 1.0);
	        if (billResult.isOK()) {
	            IBilled billed = billResult.get();
	            CoreModelServiceHolder.get().save(actEncounter);
	            if (!medicationExists) {
	                postReloadEvent(selectedArticle, billed, signature);
	            }
	        } else {
	            ResultDialog.show(billResult);
	        }
	    }
	}

	private boolean checkIfMedicationExists(List<IPrescription> prescriptions, IArticle selectedArticle,
			IArticleDefaultSignature signature) {
		String signatureDosage = signature.getSignatureAsDosisString();
		EntryType signatureEntryType = signature.getMedicationType();
		return prescriptions.stream().anyMatch(prescription -> {
			boolean sameArticle = prescription.getArticle().getId().equals(selectedArticle.getId());
			boolean sameDosage = Objects.equals(prescription.getDosageInstruction(), signatureDosage);
			boolean sameEntryType = prescription.getEntryType().equals(signatureEntryType);
			return sameArticle && sameDosage && sameEntryType;
		});
	}

	private void postReloadEvent(IArticle selectedArticle, IBilled billed, IArticleDefaultSignature signature) {
		updatePrescriptionWithBilledId(billed, signature);
		if (!signature.getMedicationType().equals(EntryType.SELF_DISPENSED)) {
			Display.getDefault().asyncExec(() -> {
				Map<String, Object> payload = Map.of(ExtensionPointConstantsUi.PAYLOAD_ARTICLE, selectedArticle,
						ExtensionPointConstantsUi.PAYLOAD_SIGNATURE, signature,
						ExtensionPointConstantsUi.PAYLOAD_BILLED, billed);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, payload);
			});
		}
		postRefreshMedicationEvent();
	}

	private void updatePrescriptionWithBilledId(IBilled billed, IArticleDefaultSignature signature) {
		getRecentPatientPrescriptions(actEncounter.getPatient()).stream().filter(prescription -> {
			Object extInfo = prescription.getExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID);
			return extInfo != null && billed.getId().equals(extInfo.toString());
		}).findFirst().ifPresent(prescription -> {
			prescription.setDosageInstruction(signature.getSignatureAsDosisString());
			prescription.setRemark(signature.getComment());
			prescription.setDateFrom(billed.getEncounter().getDate().atStartOfDay());
			CoreModelServiceHolder.get().save(prescription);
		});
	}

	private boolean checkIfMedicationTypeExists(List<IPrescription> prescriptions, IArticle selectedArticle) {
		return prescriptions.stream()
				.anyMatch(prescription -> prescription.getArticle().getId().equals(selectedArticle.getId())
						&& (prescription.getEntryType() == EntryType.FIXED_MEDICATION
								|| prescription.getEntryType() == EntryType.RESERVE_MEDICATION));
	}

	public void updatePrescriptionsWithDosage(IBilled billed) {
	    List<IPrescription> prescriptions = getRecentPatientPrescriptions(actEncounter.getPatient());
	    List<IPrescription> reversedPrescriptions = new ArrayList<>(prescriptions);
	    Collections.reverse(reversedPrescriptions);
	    Optional<IPrescription> lastPrescriptionWithDosage = reversedPrescriptions.stream()
	    	    .filter(prescription -> isMatchingPrescription(prescription, billed))
	    	    .findFirst();
	    lastPrescriptionWithDosage.ifPresent(lastPrescription -> prescriptions.stream()
	        .filter(prescription -> {
	            IArticle article = prescription.getArticle();
	            return article != null && article.getGtin() != null && billed.getCode().equals(article.getGtin());
	        })
	        .filter(prescription -> {
	            String dosageInstruction = prescription.getDosageInstruction();
	            return dosageInstruction == null || dosageInstruction.isEmpty()
	                    || !dosageInstruction.equals(lastPrescription.getDosageInstruction());
	        })
	        .forEach(prescription -> {
	            prescription.setDosageInstruction(lastPrescription.getDosageInstruction());
	            prescription.setRemark(lastPrescription.getRemark());
	            prescription.setDateFrom(actEncounter.getDate().atStartOfDay());
	            CoreModelServiceHolder.get().save(prescription);
	        }));
	}

	private boolean isMatchingPrescription(IPrescription prescription, IBilled billed) {
	    IArticle article = prescription.getArticle();
	    String dosage = prescription.getDosageInstruction();
	    return article != null && article.getGtin() != null && billed.getCode().equals(article.getGtin())
	            && dosage != null && !dosage.isEmpty();
	}

	public static List<IPrescription> getRecentPatientPrescriptions(IPatient patient) {
		return ContextServiceHolder.get().getTyped(IEncounter.class).map(IEncounter::getPatient)
				.map(p -> p.getMedicationAll(Arrays.asList(EntryType.FIXED_MEDICATION, EntryType.RESERVE_MEDICATION,
						EntryType.SYMPTOMATIC_MEDICATION, EntryType.SELF_DISPENSED)))
				.orElse(Collections.emptyList());
	}

	public void processCodeElementBlock(ICodeElementBlock block) {
		if (block instanceof IPrescription) {
			block = (ICodeElementBlock) ((IPrescription) block).getArticle();
		}
		if (block instanceof IBillable) {
			billOtherObject((IBillable) block);
		}
	}

	public void processOtherObject(Object object) {
		if (object instanceof IBillable) {
			billOtherObject((IBillable) object);
		} else if (object instanceof IDiagnosis) {
			actEncounter.addDiagnosis((IDiagnosis) object);
			CoreModelServiceHolder.get().save(actEncounter);
		}
	}

	private void billOtherObject(IBillable billable) {
		Result<?> billResult = BillingServiceHolder.get().bill(billable, actEncounter, 1.0);
		if (billResult.isOK()) {
			CoreModelServiceHolder.get().refresh(actEncounter, true);
		} else {
			ResultDialog.show(billResult);
		}
	}

	private void postRefreshMedicationEvent() {
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD,
				Map.of(ExtensionPointConstantsUi.PAYLOAD_ACTION, ExtensionPointConstantsUi.ACTION_REFRESH_MEDICATION));
	}
}
