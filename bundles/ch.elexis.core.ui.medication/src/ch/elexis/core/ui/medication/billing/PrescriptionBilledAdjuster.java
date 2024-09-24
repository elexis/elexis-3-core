package ch.elexis.core.ui.medication.billing;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IBilledAdjuster;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;

@Component
public class PrescriptionBilledAdjuster implements IBilledAdjuster {

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public void adjust(IBilled billed) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				IBillable billable = billed.getBillable();
				if (billable instanceof IArticle) {
					IArticle article = (IArticle) billable;
					Optional<IPatient> patientOpt = getPatient(billed);
					Optional<String> articleStoreToString = StoreToStringServiceHolder.get().storeToString(article);
					if (patientOpt.isPresent() && articleStoreToString.isPresent()) {
						// lookup existing prescriptions
						IQuery<IPrescription> query = CoreModelServiceHolder.get().getQuery(IPrescription.class);
						query.and(ModelPackage.Literals.IPRESCRIPTION__PATIENT, COMPARATOR.EQUALS, patientOpt.get());
						query.and("artikel", COMPARATOR.EQUALS, articleStoreToString.get()); //$NON-NLS-1$
						query.orderBy(ModelPackage.Literals.IPRESCRIPTION__DATE_FROM, ORDER.DESC);
						// create new dispensation
						boolean dispensationExists = false;
						if (!dispensationExists) {
							createDispensationPrescription(article, patientOpt.get(), billed);
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IPrescription.class);
						}
					}
				}
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
