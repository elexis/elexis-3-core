package ch.elexis.core.ui.processor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.Messages;
import ch.elexis.core.ui.dialogs.PrescriptionSignatureTitleAreaDialog;
import ch.elexis.core.ui.dialogs.ResultDialog;
import ch.rgw.tools.Result;

public class BillingProcessor {

	private final IEncounter actEncounter;

	public BillingProcessor(IEncounter actEncounter) {
		this.actEncounter = actEncounter;
	}

	public void processArticle(IArticle selectedArticle) {
		if (isArticleAlreadyBilled(selectedArticle)) {
			billArticleDirectly(selectedArticle);
			return;
		}
		List<IPrescription> prescriptions = getRecentPatientPrescriptions(actEncounter.getPatient(),
				actEncounter.getDate().atStartOfDay());
		boolean medicationExistsType = fixOrReserveWithArticleExists(prescriptions, selectedArticle);
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
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), selectedArticle);
	    dialog.lookup();
	    dialog.setFromBillingDialog(true);
		if (dialog.open() != Dialog.OK) {
			return;
		}
		IArticleDefaultSignature signature = dialog.getSignature();
		List<IPrescription> prescriptions = getRecentPatientPrescriptions(actEncounter.getPatient(),
				actEncounter.getDate().atStartOfDay());
		boolean medicationExists = checkIfMedicationExists(prescriptions, selectedArticle, signature);
		if (medicationExists) {
			return;
		}
		Result<IBilled> billResult = BillingServiceHolder.get().bill(selectedArticle, actEncounter, 1.0);
		if (!billResult.isOK()) {
			ResultDialog.show(billResult);
			return;
		}
		IBilled billed = billResult.get();
		String prescId = billed.getExtInfo(ch.elexis.core.model.verrechnet.Constants.FLD_EXT_PRESC_ID).toString();
		if (prescId != null) {
			Optional<IPrescription> prescriptionOpt = CoreModelServiceHolder.get().load(prescId, IPrescription.class);
			if (prescriptionOpt.isPresent()) {
				IPrescription prescription = prescriptionOpt.get();
				prescription.setDosageInstruction(signature.getSignatureAsDosisString());
				prescription.setRemark(signature.getComment());
				prescription.setExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID,
						billed.getId());
				prescription.setEntryType(signature.getDisposalType());
				prescription.setDateFrom(actEncounter.getDate().atStartOfDay());
				CoreModelServiceHolder.get().save(prescription);
			}
		}
		CoreModelServiceHolder.get().save(actEncounter);
		postRefreshMedicationEvent();
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

	private boolean fixOrReserveWithArticleExists(List<IPrescription> prescriptions, IArticle selectedArticle) {
		return prescriptions.stream()
				.anyMatch(prescription -> prescription.getArticle().getId().equals(selectedArticle.getId())
						&& (prescription.getEntryType() == EntryType.FIXED_MEDICATION
								|| prescription.getEntryType() == EntryType.RESERVE_MEDICATION));
	}

	public void updatePrescriptionsWithDosage(IBilled billed) {
		List<IPrescription> prescriptions = getRecentPatientPrescriptions(actEncounter.getPatient(),
				actEncounter.getDate().atStartOfDay());
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

	public static List<IPrescription> getRecentPatientPrescriptions(IPatient patient, LocalDateTime referenceDate) {
		return getMedicationRecent(patient, Arrays.asList(EntryType.FIXED_MEDICATION, EntryType.RESERVE_MEDICATION,
				EntryType.SYMPTOMATIC_MEDICATION, EntryType.SELF_DISPENSED), referenceDate);
	}

	public void processCodeElementBlock(ICodeElementBlock block) {
		List<ICodeElement> elements = block.getElements();
		List<String> notOkResults = new ArrayList<>();
		for (ICodeElement element : elements) {
			processCodeElement(element, notOkResults);
		}
		handleBillingFailures(notOkResults);
		handleUnbillableElements(block, elements);
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, actEncounter);
	}

	private void processCodeElement(ICodeElement element, List<String> notOkResults) {
		if (element instanceof IBillable) {
			billCodeElement((IBillable) element, notOkResults);
		} else if (element instanceof IDiagnosis) {
			processDiagnosis((IDiagnosis) element);
		}
	}

	private void billCodeElement(IBillable element, List<String> notOkResults) {
		Result<?> billResult = BillingServiceHolder.get().bill(element, actEncounter, 1.0);
		if (!billResult.isOK()) {
			String message = element.getCode() + " - " + ResultDialog.getResultMessage(billResult);
			if (!notOkResults.contains(message)) {
				notOkResults.add(message);
			}
		}
	}

	private void handleBillingFailures(List<String> notOkResults) {
		if (!notOkResults.isEmpty()) {
			String combinedMessages = String.join("\n", notOkResults);
			ResultDialog.show(Result.ERROR(Messages.ArticleProcessorBilledFail + combinedMessages));
		}
	}

	private void handleUnbillableElements(ICodeElementBlock block, List<ICodeElement> elements) {
		List<ICodeElement> diff = block.getDiffToReferences(elements);
		if (!diff.isEmpty()) {
			StringJoiner sb = new StringJoiner("\n");
			diff.forEach(r -> sb.add(r.toString()));
			ResultDialog.show(Result.ERROR(Messages.ArticleProcessorBilledWarningContext + sb.toString()));
		}
	}

	public void processOtherObject(Object object) {
		if (object instanceof IBillable) {
			billOtherObject((IBillable) object);
		} else if (object instanceof IDiagnosis) {
			processDiagnosis((IDiagnosis) object);
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

	private void processDiagnosis(IDiagnosis diagnosis) {
		actEncounter.addDiagnosis(diagnosis);
		CoreModelServiceHolder.get().save(actEncounter);
	}

	private void postRefreshMedicationEvent() {
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IPrescription.class);
	}

	public static List<IPrescription> getMedicationRecent(IPatient patient, List<EntryType> filterType,
			LocalDateTime referenceDate) {
		LocalDateTime startDate = referenceDate.minus(3, ChronoUnit.MONTHS);
		IQuery<IPrescription> query = CoreModelServiceHolder.get().getQuery(IPrescription.class);
		query.and(ModelPackage.Literals.IPRESCRIPTION__PATIENT, COMPARATOR.EQUALS, patient);
		query.and(ModelPackage.Literals.IPRESCRIPTION__DATE_FROM, COMPARATOR.GREATER_OR_EQUAL, startDate);
		List<IPrescription> iPrescriptions = query.execute();
		if (filterType != null && !filterType.isEmpty()) {
			return iPrescriptions.stream().filter(p -> filterType.contains(p.getEntryType()))
					.collect(Collectors.toList());
		}
		return iPrescriptions;
	}
}
