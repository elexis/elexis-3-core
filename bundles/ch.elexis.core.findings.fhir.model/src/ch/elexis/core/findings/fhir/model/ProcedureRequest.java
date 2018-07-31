package ch.elexis.core.findings.fhir.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.model.IXid;

public class ProcedureRequest
		extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.ProcedureRequest>
		implements IProcedureRequest {
	
	public ProcedureRequest(ch.elexis.core.jpa.entities.ProcedureRequest entity){
		super(entity);
	}
	
	@Override
	public String getPatientId(){
		return getEntity().getPatientId();
	}
	
	@Override
	public void setPatientId(String patientId){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				(org.hl7.fhir.dstu3.model.ProcedureRequest) resource.get();
			fhirProcedureRequest.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}
		getEntity().setPatientId(patientId);
	}
	
	@Override
	public List<ICoding> getCoding(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				(org.hl7.fhir.dstu3.model.ProcedureRequest) resource.get();
			CodeableConcept codeableConcept = fhirProcedureRequest.getCode();
			if (codeableConcept != null) {
				return ModelUtil.getCodingsFromConcept(codeableConcept);
			}
		}
		return Collections.emptyList();
	}
	
	@Override
	public void setCoding(List<ICoding> coding){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				(org.hl7.fhir.dstu3.model.ProcedureRequest) resource.get();
			CodeableConcept codeableConcept = fhirProcedureRequest.getCode();
			if (codeableConcept == null) {
				codeableConcept = new CodeableConcept();
			}
			ModelUtil.setCodingsToConcept(codeableConcept, coding);
			fhirProcedureRequest.setCode(codeableConcept);
			saveResource(resource.get());
		}
	}
	
	@Override
	public Optional<IEncounter> getEncounter(){
		String encounterId = getEntity().getEncounterId();
		if (encounterId != null && !encounterId.isEmpty()) {
			return ModelUtil.loadFinding(encounterId, IEncounter.class);
		}
		return Optional.empty();
	}
	
	@Override
	public void setEncounter(IEncounter encounter){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				(org.hl7.fhir.dstu3.model.ProcedureRequest) resource.get();
			fhirProcedureRequest
				.setContext(new Reference(new IdDt("Encounter", encounter.getId())));
			
			saveResource(resource.get());
		}
		
		String patientId = encounter.getPatientId();
		if (patientId != null && !patientId.isEmpty() && getPatientId() == null) {
			setPatientId(patientId);
		}
		
		getEntity().setEncounterId(encounter.getId());
	}
	
	@Override
	public Optional<LocalDateTime> getScheduledTime(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				(org.hl7.fhir.dstu3.model.ProcedureRequest) resource.get();
			try {
				if (fhirProcedureRequest.hasOccurrence()) {
					return Optional.of(getLocalDateTime(
						fhirProcedureRequest.getOccurrenceDateTimeType().getValue()));
				}
			} catch (FHIRException e) {
				LoggerFactory.getLogger(getClass())
					.error("Could not access scheduled time.", e);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public void setScheduledTime(LocalDateTime time){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				(org.hl7.fhir.dstu3.model.ProcedureRequest) resource.get();
			fhirProcedureRequest.setOccurrence(new DateTimeType(getDate(time)));
			
			saveResource(resource.get());
		}
	}
	
	@Override
	public RawContentFormat getRawContentFormat(){
		return RawContentFormat.FHIR_JSON;
	}
	
	@Override
	public String getRawContent(){
		return getEntity().getContent();
	}
	
	@Override
	public void setRawContent(String content){
		getEntity().setContent(content);
	}
	
	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public IXid getXid(String domain){
		// TODO Auto-generated method stub
		return null;
	}
	
}
