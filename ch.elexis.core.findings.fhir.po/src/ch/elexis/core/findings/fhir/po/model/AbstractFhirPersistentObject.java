package ch.elexis.core.findings.fhir.po.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
	
	private static FhirContext context = FhirContext.forDstu3();
	
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
	
	protected IParser getJsonParser(){
		if (parser == null) {
			parser = context.newJsonParser();
		}
		return parser;
	}
	
	protected Optional<IBaseResource> loadResource(){
		String content = get(FLD_CONTENT);
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
				set(FLD_CONTENT, resourceJson);
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
	
	protected Date getDate(LocalDateTime localDateTime){
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	protected LocalDateTime getLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
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
	public void addCoding(ICoding coding){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Optional<String> getText(){
		// TODO Auto-generated method stub
		return null;
	}
}
