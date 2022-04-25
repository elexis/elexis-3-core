package ch.elexis.core.findings.util.fhir.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Claim.DiagnosisComponent;
import org.hl7.fhir.r4.model.Claim.InsuranceComponent;
import org.hl7.fhir.r4.model.Claim.ItemComponent;
import org.hl7.fhir.r4.model.Claim.SupportingInformationComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.AbstractHelper;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Result;

@Component
public class ClaimVerrechnetTransformer implements IFhirTransformer<Claim, List<IBilled>> {

	@org.osgi.service.component.annotations.Reference
	private IFindingsService findingsService;

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService modelService;

	@org.osgi.service.component.annotations.Reference
	private IBillingService billingService;

	@org.osgi.service.component.annotations.Reference
	private ICodeElementService codeElementService;

	@Override
	public Optional<Claim> getFhirObject(List<IBilled> localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<List<IBilled>> getLocalObject(Claim fhirObject) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<List<IBilled>> updateLocalObject(Claim fhirObject, List<IBilled> localObject) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<List<IBilled>> createLocalObject(Claim fhirObject) {
		ClaimContext claimContext = new ClaimContext(fhirObject);
		// test if all the information is present
		if (claimContext.isValid()) {
			return Optional.of(claimContext.bill());
		} else {
			LoggerFactory.getLogger(ClaimVerrechnetTransformer.class)
					.warn("Could not create claim for item [" + fhirObject.getItem() + "] diagnosis ["
							+ fhirObject.getDiagnosis() + "] provider [" + fhirObject.getProvider() + "]");
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Claim.class.equals(fhirClazz) && List.class.equals(localClazz);
	}

	/**
	 * Private class used to bill an {@link Claim} item using Elexis model objects.
	 *
	 * @author thomas
	 *
	 */
	private class BillableContext {

		private Result<IBilled> lastStatus;

		private int amount;
		private IBillable billable;

		public BillableContext(IBillable billable, Integer amount) {
			this.billable = billable;
			this.amount = amount;
		}

		public int getAmount() {
			return amount;
		}

		public Result<IBilled> getLastStatus() {
			return lastStatus;
		}

		public List<IBilled> bill(ch.elexis.core.model.IEncounter behandlung, IMandator user, IMandator mandator) {
			List<IBilled> ret = new ArrayList<>();
			for (int i = 0; i < amount; i++) {
				lastStatus = billingService.bill(billable, behandlung, 1);
				if (!lastStatus.isOK()) {
					return ret;
				} else {
					ret.add(lastStatus.get());
				}
			}
			return ret;
		}
	}

	/**
	 * Private class used to bill all {@link Claim} items using Elexis model
	 * objects.
	 *
	 * @author thomas
	 *
	 */
	private class ClaimContext {

		private Claim claim;
		private List<InsuranceComponent> coverages;
		private List<ItemComponent> items;
		private List<DiagnosisComponent> diagnosis;
		private Reference providerRef;

		public ClaimContext(Claim fhirObject) {
			this.claim = fhirObject;
			this.coverages = fhirObject.getInsurance();
			this.items = fhirObject.getItem();
			this.diagnosis = fhirObject.getDiagnosis();
			this.providerRef = fhirObject.getProvider();
		}

		public boolean isValid() {
			return coverages != null && !coverages.isEmpty() && items != null && !items.isEmpty() && diagnosis != null
					&& !diagnosis.isEmpty() && providerRef != null && !providerRef.isEmpty();
		}

		public List<IBilled> bill() {
			List<IBilled> ret = new ArrayList<>();
			ICoverage coverage = getFall(coverages.get(0));
			List<IDiagnosis> diagnose = getDiagnose(diagnosis);
			Optional<ch.elexis.core.model.IEncounter> behandlung = getOrCreateBehandlung(claim, coverage, providerRef);
			if (behandlung.isPresent()) {
				ch.elexis.core.model.IEncounter cons = behandlung.get();
				Optional<IMandator> mandator = modelService.load(providerRef.getReferenceElement().getIdPart(),
						IMandator.class);
				if (mandator.isPresent()) {

					if (!cons.getCoverage().getId().equals(coverage.getId())) {
						LockResponse lockResponse = AbstractHelper.acquireLock(cons);
						if (lockResponse.isOk()) {
							cons.setCoverage(coverage);
							AbstractHelper.releaseLock(lockResponse.getLockInfo());
						}
					}

					for (IDiagnosis diag : diagnose) {
						cons.addDiagnosis(diag);
					}

					List<BillableContext> billables = getBillableContexts(items, cons);
					for (BillableContext billable : billables) {
						List<IBilled> billed = billable.bill(cons, mandator.get(), mandator.get());
						if (billed.size() < billable.getAmount()) {
							Result<IBilled> status = billable.getLastStatus();
							LoggerFactory.getLogger(ClaimVerrechnetTransformer.class)
									.error("Could not bill all items of claim. " + status);
						}
						ret.addAll(billed);
					}
					modelService.save(cons);
				}
			} else {
				LoggerFactory.getLogger(ClaimVerrechnetTransformer.class)
						.error("Could not bill items, Behandlung not found.");
			}
			return ret;
		}

		private List<BillableContext> getBillableContexts(List<ItemComponent> items,
				ch.elexis.core.model.IEncounter cons) {
			List<BillableContext> ret = new ArrayList<>();
			for (ItemComponent itemComponent : items) {
				CodeableConcept serviceCoding = itemComponent.getProductOrService();
				if (serviceCoding != null) {
					for (Coding coding : serviceCoding.getCoding()) {
						Optional<IBillable> billable = getBillable(coding.getSystem(), coding.getCode(), cons);
						Optional<Integer> amount = getAmount(itemComponent);
						if (billable.isPresent() && amount.isPresent()) {
							ret.add(new BillableContext(billable.get(), amount.get()));
							break;
						}
					}
				}
			}
			return ret;
		}

		private Optional<Integer> getAmount(ItemComponent itemComponent) {
			Quantity quantity = itemComponent.getQuantity();
			if (quantity != null) {
				return Optional.of(quantity.getValue().intValue());
			}
			return Optional.empty();
		}

		private Optional<IBillable> getBillable(String system, String code, ch.elexis.core.model.IEncounter cons) {
			if (system.equals(CodingSystem.ELEXIS_TARMED_CODESYSTEM.getSystem())) {
				Map<Object, Object> context = new HashMap<>();
				context.put(ICodeElementService.ContextKeys.CONSULTATION, cons);
				Optional<ICodeElement> tarmed = codeElementService.loadFromString("Tarmed", code, context);
				if (tarmed.isPresent()) {
					return tarmed.filter(IBillable.class::isInstance).map(IBillable.class::cast);
				}
			}
			LoggerFactory.getLogger(ClaimVerrechnetTransformer.class)
					.warn("Could not find billable for system [" + system + "] code [" + code + "]");
			return Optional.empty();
		}

		private List<IDiagnosis> getDiagnose(List<DiagnosisComponent> diagnosis) {
			List<IDiagnosis> ret = new ArrayList<>();
			for (DiagnosisComponent diagnosisComponent : diagnosis) {
				if (diagnosisComponent.hasDiagnosisCodeableConcept()) {
					CodeableConcept diagnoseCoding = (CodeableConcept) diagnosisComponent.getDiagnosis();
					if (diagnoseCoding != null) {
						for (Coding coding : diagnoseCoding.getCoding()) {
							IDiagnosisReference diag = modelService.create(IDiagnosisReference.class);
							diag.setCode(coding.getCode());
							diag.setText((coding.getDisplay() != null) ? coding.getDisplay() : "MISSING");
							if (CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem().equals(coding.getSystem())) {
								diag.setReferredClass("ch.elexis.data.TICode");
							}
							ret.add(diag);
						}
					}
				}
			}
			return ret;
		}

		private ICoverage getFall(InsuranceComponent insuranceComponent) {
			Reference reference = insuranceComponent.getCoverage();
			if (reference != null && !reference.isEmpty()) {
				Optional<ICoverage> fallOpt = modelService.load(reference.getReferenceElement().getIdPart(),
						ICoverage.class);
				return fallOpt.orElse(null);
			}
			return null;
		}

		private Optional<ch.elexis.core.model.IEncounter> getOrCreateBehandlung(Claim fhirObject, ICoverage fall,
				Reference providerRef) {
			if (fhirObject.getSupportingInfo() != null && !fhirObject.getSupportingInfo().isEmpty()) {
				List<SupportingInformationComponent> information = fhirObject.getSupportingInfo();
				for (SupportingInformationComponent specialConditionComponent : information) {
					Type value = specialConditionComponent.getValue();
					if (value instanceof StringType) {
						if (((StringType) value).getValue().startsWith("Encounter/")) {
							Optional<ch.elexis.core.model.IEncounter> found = getBehandlungWithEncounterRef(
									new IdType(((StringType) value).getValue()));
							if (found.isPresent()) {
								return found;
							}
						}
					}
				}
			}
			// if not found create
			// Optional<Kontakt> mandator = KontaktService.INSTANCE
			// .findById(providerRef.getReferenceElement().getIdPart());
			// if (mandator.isPresent()) {
			// return Optional.of(BehandlungService.INSTANCE.create(fall,
			// mandator.get()));
			// }
			return Optional.empty();
		}

		private Optional<ch.elexis.core.model.IEncounter> getBehandlungWithEncounterRef(IdType encounterRef) {
			if (encounterRef.getIdPart() != null) {
				Optional<IEncounter> encounter = findingsService.findById(encounterRef.getIdPart(), IEncounter.class);
				if (encounter.isPresent()) {
					return getBehandlungForEncounter(encounter.get());
				}
			}
			return Optional.empty();
		}

		private Optional<ch.elexis.core.model.IEncounter> getBehandlungForEncounter(IEncounter encounter) {
			return modelService.load((encounter).getConsultationId(), ch.elexis.core.model.IEncounter.class);
		}
	}
}
