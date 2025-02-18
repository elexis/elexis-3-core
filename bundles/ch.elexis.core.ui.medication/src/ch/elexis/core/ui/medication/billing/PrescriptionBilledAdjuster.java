package ch.elexis.core.ui.medication.billing;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IBilledAdjuster;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.processor.BillingProcessor;

@Component
public class PrescriptionBilledAdjuster implements IBilledAdjuster {
	private BillingProcessor billingProcessor;
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public void adjust(IBilled billed) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				IBillable billable = billed.getBillable();
				billingProcessor = new BillingProcessor(billed.getEncounter());
				if (billable instanceof IArticle) {
					IArticle article = (IArticle) billable;
					Optional<IPatient> patientOpt = getPatient(billed);
					Optional<String> articleStoreToString = StoreToStringServiceHolder.get().storeToString(article);
					if (patientOpt.isPresent() && articleStoreToString.isPresent()) {
							createDispensationPrescription(article, patientOpt.get(), billed);
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IPrescription.class);
					}
				}
				billingProcessor.updatePrescriptionsWithDosage(billed);
			}
		});
	}

	private Optional<IPatient> getPatient(IBilled billed) {
		IEncounter encounter = billed.getEncounter();
		if (encounter != null) {
			ICoverage coverage = encounter.getCoverage();
			if (coverage != null) {
				return Optional.of(coverage.getPatient());
			}
		}
		return Optional.empty();
	}

	private IPrescription createDispensationPrescription(IArticle article, IPatient patient, IBilled billed) {
		IPrescription prescription = new IPrescriptionBuilder(CoreModelServiceHolder.get(), ContextServiceHolder.get(),
				article, patient, StringUtils.EMPTY).build();
		prescription.setExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID, billed.getId());
		billed.setExtInfo(ch.elexis.core.model.verrechnet.Constants.FLD_EXT_PRESC_ID, prescription.getId());
		prescription.setEntryType(EntryType.SELF_DISPENSED);
		prescription.setDateFrom(billed.getEncounter().getDate().atStartOfDay());
		CoreModelServiceHolder.get().save(prescription);
		return prescription;
	}
}
