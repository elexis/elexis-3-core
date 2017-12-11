package ch.elexis.core.findings.fhir.po.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.util.fhir.accessor.ConditionAccessor;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.VersionInfo;

public class Condition extends AbstractFhirPersistentObject implements ICondition {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_CONDITION";
	protected static final String VERSION = "1.0.0";
	
	public static final String FLD_PATIENTID = "patientid"; //$NON-NLS-1$
	
	private ConditionAccessor accessor = new ConditionAccessor();
	
	//@formatter:off
	protected static final String createDB =
	"CREATE TABLE " + TABLENAME + "(" +
	"ID					VARCHAR(25) PRIMARY KEY," +
	"lastupdate 		BIGINT," +
	"deleted			CHAR(1) default '0'," + 
	"patientid	        VARCHAR(80)," +
	"content      		TEXT" + ");" + 
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_CONDITION_IDX1 ON " + TABLENAME + " (patientid);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_PATIENTID + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_PATIENTID, FLD_CONTENT);
		
		Condition version = load("VERSION");
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
	
	public static Condition load(final String id){
		return new Condition(id);
	}
	
	protected Condition(final String id){
		super(id);
	}
	
	public Condition(){
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
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setPatientId((DomainResource) resource.get(), patientId);
			saveResource(resource.get());
		}
	}
	
	@Override
	public Optional<LocalDate> getDateRecorded(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getDateRecorded((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public void setDateRecorded(LocalDate date){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setDateRecorded((DomainResource) resource.get(), date);
			saveResource(resource.get());
		}
	}
	
	@Override
	public ConditionCategory getCategory(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCategory((DomainResource) resource.get());
		}
		return ConditionCategory.UNKNOWN;
	}
	
	@Override
	public void setCategory(ConditionCategory category){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCategory((DomainResource) resource.get(), category);
			saveResource(resource.get());
		}
	}
	
	@Override
	public ConditionStatus getStatus(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStatus((DomainResource) resource.get());
		}
		return ConditionStatus.UNKNOWN;
	}
	
	@Override
	public void setStatus(ConditionStatus status){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStatus((DomainResource) resource.get(), status);
			saveResource(resource.get());
		}
	}
	
	@Override
	public void setStart(String start){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStart((DomainResource) resource.get(), start);
			saveResource(resource.get());
		}
	}
	
	@Override
	public Optional<String> getStart(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStart((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public void setEnd(String end){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEnd((DomainResource) resource.get(), end);
			saveResource(resource.get());
		}
	}
	
	@Override
	public Optional<String> getEnd(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEnd((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public void addNote(String text){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.addNote((DomainResource) resource.get(), text);
			saveResource(resource.get());
		}
	}
	
	@Override
	public void removeNote(String text){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.removeNote((DomainResource) resource.get(), text);
			saveResource(resource.get());
		}
	}
	
	@Override
	public List<String> getNotes(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getNotes((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<ICoding> getCoding(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCoding((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}
	
	@Override
	public void setCoding(List<ICoding> coding){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCoding((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}
}
