package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.FindingsContentHelper;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.AbstractHelper;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.VersionedResource;

/**
 * STU3: ProcedureRequest -> R4: ServiceRequest
 */
@Component
public class ServiceRequestIProcedureRequestTransformer implements IFhirTransformer<ServiceRequest, IProcedureRequest> {

	private static Logger logger = LoggerFactory.getLogger(ServiceRequestIProcedureRequestTransformer.class);

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IFindingsService findingsService;

	@Reference
	private ICodingService codingService;

	private FindingsContentHelper contentHelper;

	public void activate() {
		contentHelper = new FindingsContentHelper();
	}

	@Override
	public Optional<ServiceRequest> getFhirObject(IProcedureRequest localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((ServiceRequest) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> getLocalObject(ServiceRequest fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IProcedureRequest> existing = findingsService.findById(fhirObject.getId(),
					IProcedureRequest.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> updateLocalObject(ServiceRequest fhirObject, IProcedureRequest localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> createLocalObject(ServiceRequest fhirObject) {
		IProcedureRequest iProcedureRequest = findingsService.create(IProcedureRequest.class);
		contentHelper.setResource(fhirObject, iProcedureRequest);
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			String id = fhirObject.getSubject().getReferenceElement().getIdPart();
			Optional<IPatient> patient = modelService.load(id, IPatient.class);
			patient.ifPresent(k -> iProcedureRequest.setPatientId(id));
		}
		IEncounter iEncounter = null;
		if (fhirObject.getEncounter() != null && fhirObject.getEncounter().hasReference()) {
			String id = fhirObject.getEncounter().getReferenceElement().getIdPart();
			Optional<IEncounter> encounter = findingsService.findById(id, IEncounter.class);
			if (encounter.isPresent()) {
				iEncounter = encounter.get();
				iProcedureRequest.setEncounter(iEncounter);
			}
		}
		findingsService.saveFinding(iProcedureRequest);
		if (iEncounter != null) {
			writeBehandlungSoapText(iEncounter, fhirObject);
		}
		return Optional.of(iProcedureRequest);
	}

	private void writeBehandlungSoapText(IEncounter iEncounter, ServiceRequest procedureRequest) {
		Optional<ch.elexis.core.model.IEncounter> behandlung = modelService.load(iEncounter.getConsultationId(),
				ch.elexis.core.model.IEncounter.class);
		behandlung.ifPresent(cons -> {
			LockResponse lockresponse = AbstractHelper.acquireLock(cons);
			if (lockresponse.isOk()) {
				String subjectivText = getSubjectiveText(iEncounter);
				String assessmentText = getAssessmentText(iEncounter);
				String procedureText = getProcedureText(behandlung.get());

				StringBuilder text = new StringBuilder();
				if (!subjectivText.isEmpty()) {
					text.append("A:\n" + subjectivText);
				}
				if (!assessmentText.isEmpty()) {
					if (text.length() > 0) {
						text.append("\n\n");
					}
					text.append("B:\n" + assessmentText);
				}
				if (!procedureText.isEmpty()) {
					if (text.length() > 0) {
						text.append("\n\n");
					}
					text.append("P:\n" + procedureText);
				}

				logger.debug("Updating SOAP text of cons [" + cons.getId() + "]\n" + text.toString());

				VersionedResource vResource = VersionedResource.load(null);
				vResource.update(text.toString(), "From FHIR");
				cons.setVersionedEntry(vResource);
				modelService.save(cons);
				AbstractHelper.releaseLock(lockresponse.getLockInfo());
			}
		});
	}

	private String getProcedureText(ch.elexis.core.model.IEncounter behandlung) {
		StringBuilder ret = new StringBuilder();
		@SuppressWarnings("unchecked")
		List<IProcedureRequest> procedureRequests = (findingsService.getConsultationsFindings(behandlung.getId(),
				IProcedureRequest.class));
		if (procedureRequests != null && !procedureRequests.isEmpty()) {
			for (IProcedureRequest iProcedureRequest : procedureRequests) {
				Optional<String> text = iProcedureRequest.getText();
				text.ifPresent(t -> {
					if (ret.length() > 0) {
						ret.append(StringUtils.LF);
					}
					ret.append(t);
				});
			}
		}
		return ret.toString();
	}

	private String getAssessmentText(IEncounter iEncounter) {
		List<ICondition> indication = iEncounter.getIndication();
		StringBuilder ret = new StringBuilder();
		for (ICondition iCondition : indication) {
			List<ICoding> coding = iCondition.getCoding();
			Optional<String> text = iCondition.getText();
			ConditionCategory category = iCondition.getCategory();
			if (category == ConditionCategory.PROBLEMLISTITEM) {
				boolean hasText = text.isPresent() && !text.get().isEmpty();
				if (ret.length() > 0) {
					ret.append(StringUtils.LF);
				}
				if (hasText) {
					ret.append(text.orElse(StringUtils.EMPTY));
				}
				if (coding != null && !coding.isEmpty()) {
					if (hasText) {
						ret.append(", ");
					}
					for (ICoding iCoding : coding) {
						ret.append(codingService.getLabel(iCoding));
					}
				}
			}
		}
		return ret.toString();
	}

	private String getSubjectiveText(IEncounter iEncounter) {
		List<IObservation> observations = findingsService.getConsultationsFindings(iEncounter.getConsultationId(),
				IObservation.class);
		StringBuilder ret = new StringBuilder();
		for (IObservation iObservation : observations) {
			if (iObservation.getCategory() == ObservationCategory.SOAP_SUBJECTIVE) {
				Optional<String> text = iObservation.getText();
				if (ret.length() > 0) {
					ret.append(StringUtils.LF);
				}
				ret.append(text.orElse(StringUtils.EMPTY));
			}
		}
		return ret.toString();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return ServiceRequest.class.equals(fhirClazz) && IProcedureRequest.class.equals(localClazz);
	}

}
