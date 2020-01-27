package ch.elexis.core.findings.fhir.model;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IXid;

public class DocumentReference
		extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.DocumentReference>
		implements IDocumentReference {
	
	//	private DocumentReferenceAccessor accessor = new DocumentReferenceAccessor();
	
	public DocumentReference(ch.elexis.core.jpa.entities.DocumentReference entity){
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
			org.hl7.fhir.dstu3.model.DocumentReference fhirDocumentReference =
				(org.hl7.fhir.dstu3.model.DocumentReference) resource
					.get();
			fhirDocumentReference.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}
		
		getEntity().setPatientId(patientId);
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
	public IDocument getDocument(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setDocument(IDocument document){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ICoding getDocumentClass(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setDocumentClass(ICoding coding){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ICoding getPracticeSetting(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setPracticeSetting(ICoding coding){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ICoding getFacilityType(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setFacilityType(ICoding coding){
		// TODO Auto-generated method stub
		
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
