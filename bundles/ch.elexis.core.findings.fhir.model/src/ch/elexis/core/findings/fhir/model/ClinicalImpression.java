package ch.elexis.core.findings.fhir.model;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.model.IXid;

public class ClinicalImpression
		extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.ClinicalImpression>
		implements IClinicalImpression {
	
	public ClinicalImpression(ch.elexis.core.jpa.entities.ClinicalImpression entity){
		super(entity);
	}
	
	@Override
	public String getPatientId(){
		return getEntity().getPatientid();
	}
	
	@Override
	public void setPatientId(String patientId){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ClinicalImpression fhirClinicalImpression = (org.hl7.fhir.dstu3.model.ClinicalImpression) resource
					.get();
			fhirClinicalImpression.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}
		
		getEntity().setPatientid(patientId);
	}
	
	@Override
	public Optional<IEncounter> getEncounter(){
		String encounterId = getEntity().getEncounterid();
		if (encounterId != null && !encounterId.isEmpty()) {
			return ModelUtil.loadFinding(encounterId, IEncounter.class);
		}
		return Optional.empty();
	}
	
	@Override
	public void setEncounter(IEncounter encounter){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ClinicalImpression fhirClinicalImpression =
				(org.hl7.fhir.dstu3.model.ClinicalImpression) resource.get();
			fhirClinicalImpression
				.setContext(new Reference(new IdDt("Encounter", encounter.getId())));
			saveResource(resource.get());
		}
		
		String patientId = encounter.getPatientId();
		if (patientId != null && !patientId.isEmpty() && getPatientId() == null) {
			setPatientId(patientId);
		}
		
		getEntity().setEncounterid(encounter.getId());
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
