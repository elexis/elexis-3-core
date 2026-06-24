package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.accessor.ConditionAccessor;
import ch.elexis.core.findings.util.fhir.transformer.helper.AbstractHelper;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.FindingsContentHelper;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
@Component
public class ConditionIConditionTransformer implements IFhirTransformer<Condition, ICondition> {

	@Inject
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService modelService;

	@Inject
	@Reference
	IFindingsService findingsService;

	private ConditionAccessor accessor = new ConditionAccessor();

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	@Override
	public Optional<Condition> getFhirObject(ICondition localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			Condition fhirObject = (Condition) resource.get();
			FhirUtil.setVersionedIdPartLastUpdatedMeta(Condition.class, fhirObject, localObject);
			return Optional.of(fhirObject);
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> getLocalObject(Condition fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<ICondition> existing = findingsService
					.findById(FhirUtil.getLocalId(fhirObject.getId()).orElse(StringUtils.EMPTY), ICondition.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> updateLocalObject(Condition fhirObject, ICondition localObject) {
		Optional<String> fhirText = ModelUtil.getNarrativeAsString(fhirObject.getText());
		if (fhirText.isPresent()) {
			localObject.setText(fhirText.get());
		} else {
			localObject.setText(StringUtils.EMPTY);
		}
		localObject.setStatus(accessor.getStatus(fhirObject));
		localObject.setStart(accessor.getStart(fhirObject).orElse(StringUtils.EMPTY));
		localObject.setEnd(accessor.getEnd(fhirObject).orElse(StringUtils.EMPTY));

		findingsService.saveFinding(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<ICondition> createLocalObject(Condition fhirObject) {
		ICondition iCondition = findingsService.create(ICondition.class);
		contentHelper.setResource(fhirObject, iCondition);
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			String id = fhirObject.getSubject().getReferenceElement().getIdPart();
			Optional<IPatient> patient = modelService.load(id, IPatient.class);
			patient.ifPresent(k -> iCondition.setPatientId(id));
		}
		findingsService.saveFinding(iCondition);

		// Additionally bridge into the Elexis client world if an Encounter context
		// is referenced: the FHIR `Condition.encounter` is interpreted as a write
		// anchor that should produce a row in the `behdl_dg_joint` junction table
		// so that the desktop client, TARDOC billing, history view etc. actually
		// see this diagnosis. The mapping mirrors the established pattern used by
		// ClaimVerrechnetTransformer for `Claim.diagnosis[]`.
		persistAsClientDiagnosis(fhirObject);

		return Optional.of(iCondition);
	}

	/**
	 * Bridge a FHIR Condition into the Elexis client diagnosis world
	 * (`diagnosen` master + `behdl_dg_joint` junction).
	 *
	 * <p>
	 * The Findings world (`ch_elexis_core_findings_condition`) is kept as the
	 * canonical store for the FHIR roundtrip; this method only adds the
	 * additional junction link expected by the desktop client. If any of the
	 * required inputs (encounter reference, coding) is missing, the method is a
	 * no-op so that other Condition use-cases (problem list, family history,
	 * etc.) remain unaffected.
	 * </p>
	 */
	private void persistAsClientDiagnosis(Condition fhirObject) {
		if (fhirObject == null || !fhirObject.hasEncounter() || !fhirObject.hasCode()) {
			return;
		}
		String fhirEncounterId = fhirObject.getEncounter().getReferenceElement().getIdPart();
		if (StringUtils.isBlank(fhirEncounterId)) {
			return;
		}
		// Two-step lookup identical to ChargeItemIBilledTransformer.assertEncounter:
		//   FHIR-Encounter id -> ch_elexis_core_findings_encounter.CONSULTATIONID -> behandlungen.ID
		Optional<ch.elexis.core.findings.IEncounter> findingsEncounter = findingsService.findById(fhirEncounterId,
				ch.elexis.core.findings.IEncounter.class);
		Optional<ch.elexis.core.model.IEncounter> consultation = findingsEncounter
				.flatMap(f -> modelService.load(f.getConsultationId(), ch.elexis.core.model.IEncounter.class));
		if (!consultation.isPresent()) {
			LoggerFactory.getLogger(ConditionIConditionTransformer.class).warn(
					"Condition.encounter='{}' could not be resolved to an Elexis Behandlung; skipping junction write.",
					fhirEncounterId);
			return;
		}

		if (!fhirObject.getCode().hasCoding() || fhirObject.getCode().getCoding().isEmpty()) {
			return;
		}

		Logger log = LoggerFactory.getLogger(ConditionIConditionTransformer.class);
		ch.elexis.core.model.IEncounter cons = consultation.get();
		LockResponse lockResponse = AbstractHelper.acquireLock(cons);
		try {
			for (Coding coding : fhirObject.getCode().getCoding()) {
				if (coding == null || StringUtils.isBlank(coding.getCode())) {
					continue;
				}
				IDiagnosisReference diag = modelService.create(IDiagnosisReference.class);
				diag.setCode(coding.getCode());
				diag.setText(StringUtils.isNotBlank(coding.getDisplay()) ? coding.getDisplay() : coding.getCode());
				diag.setReferredClass(mapCodingSystemToReferredClass(coding.getSystem()));
				// Saving the diagnosis reference + the encounter triggers
				// AbstractModelService.save -> ContextService.postEvent, which is not
				// implemented on the headless server (UnsupportedOperationException).
				// The JPA write itself succeeds (junction row is committed); swallow
				// the post-save event failure so the HTTP request still returns 201.
				safeSave(diag, log);
				cons.addDiagnosis(diag);
				log.debug("Linked Condition coding system='{}' code='{}' to Behandlung '{}'.",
						coding.getSystem(), coding.getCode(), cons.getId());
			}
			safeSave(cons, log);
		} finally {
			if (lockResponse != null && lockResponse.isOk()) {
				AbstractHelper.releaseLock(lockResponse.getLockInfo());
			}
		}
	}

	/**
	 * Wrap modelService.save() so that the headless server's missing event-bus
	 * (ContextService.postEvent throws UnsupportedOperationException) does not
	 * surface as an HTTP 500 to the FHIR client. The actual JPA write has
	 * already happened at that point and the row is persisted.
	 */
	private void safeSave(Object entity, Logger log) {
		try {
			modelService.save((ch.elexis.core.model.Identifiable) entity);
		} catch (UnsupportedOperationException uoe) {
			log.debug("modelService.save post-event raised UnsupportedOperationException; "
					+ "persistence row is already committed, ignoring. entity={}", entity, uoe);
		}
	}

	/**
	 * Map a FHIR Coding.system URI to the Elexis `ReferredClass` value used in
	 * the `diagnosen.KLASSE` column. Identical to the resolution used by
	 * ClaimVerrechnetTransformer; additionally accepts the HAPI-default
	 * "http://hl7.org/fhir/sid/icd-10" URI (without the German `-de` suffix) so
	 * that off-the-shelf FHIR clients also produce ICD-10 mappings instead of
	 * silently falling through to FreeTextDiagnose.
	 */
	static String mapCodingSystemToReferredClass(String system) {
		if (system != null && CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem().equals(system)) {
			return "ch.elexis.data.TICode"; //$NON-NLS-1$
		}
		if (system != null && (CodingSystem.ICD_DE_CODESYSTEM.getSystem().equals(system)
				|| "http://hl7.org/fhir/sid/icd-10".equals(system))) { //$NON-NLS-1$
			return "ch.elexis.data.ICD10"; //$NON-NLS-1$
		}
		return "ch.elexis.data.FreeTextDiagnose"; //$NON-NLS-1$
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Condition.class.equals(fhirClazz) && ICondition.class.equals(localClazz);
	}

}
