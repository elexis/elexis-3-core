package ch.elexis.core.findings.fhir.po.model;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.data.PersistentObject;

public abstract class AbstractFhirPersistentObject extends PersistentObject implements IFinding {
	
	private static final Logger logger =
		LoggerFactory.getLogger(AbstractFhirPersistentObject.class);
	
	public static final String FLD_CONTENT = "content"; //$NON-NLS-1$
	
	private static FhirContext context;
	
	private IParser parser;
	
	public AbstractFhirPersistentObject(){
	}
	
	public AbstractFhirPersistentObject(String id){
		super(id);
	}
	
	public AbstractFhirPersistentObject create(){
		super.create(null);
		return this;
	}
	
	protected synchronized IParser getJsonParser(){
		if(context == null) {
			context = FhirContext.forDstu3();
		}
		if (parser == null) {
			parser = context.newJsonParser();
		}
		return parser;
	}
	
	protected Optional<IBaseResource> loadResource(){
		String content = getRawContent();
		IBaseResource resource = null;
		if (content != null && !content.isEmpty()) {
			try {
				resource = getJsonParser().parseResource(content);
			} catch (DataFormatException ex) {
				logger.error("Could not load resource [" + this + "]", ex);
			}
		}
		return Optional.ofNullable(resource);
	}
	
	protected void saveResource(IBaseResource resource){
		if (resource != null) {
			try {
				String resourceJson = getJsonParser().encodeResourceToString(resource);
				setRawContent(resourceJson);
			} catch (DataFormatException ex) {
				logger.error("Could not save resource [" + this + "]", ex);
			}
		}
	}
	
	/**
	 * Default implementation for getting the encounter from a finding. Not all findings have an
	 * encounter. But many, so a default implementation makes sense.
	 * 
	 * @param encounterIdField
	 * @return
	 */
	protected Optional<IEncounter> getEncounter(String encounterIdField){
		String encounterId = get(encounterIdField);
		if (encounterId != null && !encounterId.isEmpty()) {
			Encounter encounter = Encounter.load(encounterId);
			if (encounter.exists()) {
				return Optional.of(encounter);
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Default implementation for setting the encounter from a finding. Not all findings have an
	 * encounter. But many, so a default implementation makes sense.
	 * 
	 * @param encounter
	 * @param encounterIdField
	 */
	protected void setEncounter(IEncounter encounter, String encounterIdField){
		set(encounterIdField, encounter.getId());
		String patientId = encounter.getPatientId();
		if (patientId != null && !patientId.isEmpty()) {
			setPatientId(patientId);
		}
	}
	
	@Override
	public String getId(){
		return super.getId();
	}
	
	@Override
	public List<ICoding> getCoding(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setCoding(List<ICoding> coding){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Optional<String> getText(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent() && resource.get() instanceof DomainResource) {
			Narrative narrative = ((DomainResource) resource.get()).getText();
			if (narrative != null && narrative.getDivAsString() != null) {
				String text = narrative.getDivAsString();
				if (text != null) {
					String divDecodedText = text.replaceAll(
						"<div>|<div xmlns=\"http://www.w3.org/1999/xhtml\">|</div>|</ div>", "");
					divDecodedText = divDecodedText.replaceAll("<br/>|<br />", "\n");
					return Optional.of(divDecodedText);
				}
			}
		}
		return Optional.empty();
	}
	
	@Override
	public void setText(String text){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent() && resource.get() instanceof DomainResource) {
			DomainResource domainResource = (DomainResource) resource.get();
			Narrative narrative = domainResource.getText();
			if (narrative != null && narrative.getDivAsString() != null) {
				narrative = new Narrative();
			}
			String divEncodedText = text.replaceAll("(\r\n|\r|\n)", "<br />");
			narrative.setDivAsString(divEncodedText);
			domainResource.setText(narrative);
			saveResource(domainResource);
		}
	}
	
	public RawContentFormat getRawContentFormat(){
		return RawContentFormat.FHIR_JSON;
	}
	
	public String getRawContent(){
		return get(FLD_CONTENT);
	}
	
	public void setRawContent(String content){
		set(FLD_CONTENT, content);
	}
}
