package ch.elexis.core.findings.fhir.po.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.fhir.po.service.FindingsService;
import ch.elexis.core.findings.util.fhir.accessor.EncounterAccessor;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.VersionInfo;

public class Encounter extends AbstractFhirPersistentObject implements IEncounter {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_ENCOUNTER";
	protected static final String VERSION = "1.0.0";
	
	public static final String FLD_PATIENTID = "patientid"; //$NON-NLS-1$
	public static final String FLD_MANDATORID = "mandatorid"; //$NON-NLS-1$
	public static final String FLD_CONSULTATIONID = "consultationid"; //$NON-NLS-1$
	
	private EncounterAccessor accessor = new EncounterAccessor();
	
	//@formatter:off
	protected static final String createDB =
	"CREATE TABLE " + TABLENAME + "(" +
	"ID					VARCHAR(25) PRIMARY KEY," +
	"lastupdate 		BIGINT," +
	"deleted			CHAR(1) default '0'," + 
	"patientid	        VARCHAR(80)," +
	"mandatorid         VARCHAR(80)," +
	"consultationid     VARCHAR(80)," +
	"content      		TEXT" + ");" + 
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_ENCOUNTER_IDX1 ON " + TABLENAME + " (patientid);" +
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_ENCOUNTER_IDX2 ON " + TABLENAME + " (consultationid);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_PATIENTID + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_PATIENTID, FLD_MANDATORID, FLD_CONSULTATIONID,
			FLD_CONTENT);
		
		Encounter version = load("VERSION");
		if (version.state() < PersistentObject.DELETED) {
			createOrModifyTable(createDB);
		} else {
			VersionInfo vi = new VersionInfo(version.get(FLD_PATIENTID));
			if (vi.isOlder(VERSION)) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				version.set(FLD_PATIENTID, VERSION);
			}
		}
	}
	
	public static Encounter load(final String id){
		return new Encounter(id);
	}
	
	protected Encounter(final String id){
		super(id);
	}
	
	public Encounter(){
	}
	
	@Override
	public String getLabel(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@Override
	public String getPatientId(){
		return get(FLD_PATIENTID);
	}
	
	@Override
	public void setPatientId(String patientId){
		set(FLD_PATIENTID, patientId);
	}
	
	@Override
	public String getConsultationId(){
		return get(FLD_CONSULTATIONID);
	}
	
	@Override
	public void setConsultationId(String consultationId){
		set(FLD_CONSULTATIONID, consultationId);
	}
	
	@Override
	public String getMandatorId(){
		return get(FLD_MANDATORID);
	}
	
	@Override
	public void setMandatorId(String serviceProviderId){
		set(FLD_MANDATORID, serviceProviderId);
	}
	
	@Override
	public Optional<LocalDateTime> getStartTime(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStartTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public void setStartTime(LocalDateTime time){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStartTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}
	
	@Override
	public Optional<LocalDateTime> getEndTime(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEndTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public void setEndTime(LocalDateTime time){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEndTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}
	
	@Override
	public List<ICondition> getIndication(){
		List<ICondition> indication = new ArrayList<>();
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getIndication((DomainResource) resource.get(), new FindingsService());
		}
		return indication;
	}
	
	@Override
	public void setIndication(List<ICondition> indication){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setIndication((DomainResource) resource.get(), indication);
			saveResource(resource.get());
		}
	}
	
	@Override
	public List<ICoding> getType(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getType((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}
	
	@Override
	public void setType(List<ICoding> coding){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setType((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}
}
