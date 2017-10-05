package ch.elexis.core.findings.fhir.po.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.util.fhir.accessor.ObservationAccessor;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.VersionInfo;

public class Observation extends AbstractFhirPersistentObject implements IObservation {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_OBSERVATION";
	protected static final String VERSION = "1.0.1";
	
	public static final String FLD_PATIENTID = "patientid"; //$NON-NLS-1$
	public static final String FLD_ENCOUNTERID = "encounterid"; //$NON-NLS-1$
	public static final String FLD_PERFORMERID = "performerid"; //$NON-NLS-1$
	public static final String FLD_TYPE = "type";
	public static final String FLD_REFERENCED = "referenced";
	
	private ObservationAccessor accessor = new ObservationAccessor();
	
	//@formatter:off
	protected static final String createDB =
	"CREATE TABLE " + TABLENAME + "(" +
	"ID					VARCHAR(25) PRIMARY KEY," +
	"lastupdate 		BIGINT," +
	"deleted			CHAR(1) default '0'," + 
	"type				CHAR(8), " +
	"referenced			CHAR(1) default '0'," +		
	"patientid	        VARCHAR(80)," +
	"encounterid	    VARCHAR(80)," +
	"performerid	    VARCHAR(80)," +
	"content      		TEXT" + ");" + 
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATION_IDX1 ON " + TABLENAME + " (patientid);" +
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATION_IDX2 ON " + TABLENAME + " (encounterid);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_PATIENTID + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_PATIENTID, FLD_ENCOUNTERID, FLD_PERFORMERID, FLD_CONTENT,
			FLD_TYPE, FLD_REFERENCED);
		
		Observation version = load("VERSION");
		if (version.state() < PersistentObject.DELETED) {
			createOrModifyTable(createDB);
		} else {
			VersionInfo vi = new VersionInfo(version.get(FLD_PATIENTID));
			if (vi.isOlder(VERSION)) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				createOrModifyTable(
					"ALTER TABLE " + TABLENAME + " ADD " + FLD_TYPE + " CHAR(8);");
				createOrModifyTable(
					"ALTER TABLE " + TABLENAME + " ADD " + FLD_REFERENCED
						+ " CHAR(1) default '0';");
				version.set(FLD_PATIENTID, VERSION);
			}
		}
	}
	
	public static Observation load(final String id){
		return new Observation(id);
	}
	
	protected Observation(final String id){
		super(id);
	}
	
	public Observation(){}
	
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
	public Optional<IEncounter> getEncounter(){
		return getEncounter(FLD_ENCOUNTERID);
	}
	
	@Override
	public void setEncounter(IEncounter encounter){
		setEncounter(encounter, FLD_ENCOUNTERID);
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
	public List<IObservation> getSourceObservations(ObservationLinkType type){
		Query<ObservationLink> qbe = new Query<>(ObservationLink.class);
		qbe.add(ObservationLink.FLD_TARGETID, Query.EQUALS, getId());
		qbe.add(ObservationLink.FLD_TYPE, Query.EQUALS, type.name());
		qbe.orderBy(true, ObservationLink.FLD_LASTUPDATE);
		
		List<ObservationLink> observationLinks = qbe.execute();
		List<IObservation> iObservations = new ArrayList<>();
		for (ObservationLink link : observationLinks)
		{
			String id = link.get(ObservationLink.FLD_SOURCEID);
			iObservations.add(Observation.load(id));
		}
		return iObservations;
	}
	
	@Override
	public void addSourceObservation(IObservation source, ObservationLinkType type){
		if (source != null && source.getId() != null && getId() != null) {
			ObservationLink observationLink = new ObservationLink();
			observationLink.set(ObservationLink.FLD_TARGETID, getId());
			observationLink.set(ObservationLink.FLD_SOURCEID, source.getId());
			observationLink.set(ObservationLink.FLD_TYPE, type.name());
		}
	}
	
	@Override
	public List<IObservation> getTargetObseravtions(ObservationLinkType type){
		Query<ObservationLink> qbe = new Query<>(ObservationLink.class);
		qbe.add(ObservationLink.FLD_SOURCEID, Query.EQUALS, getId());
		qbe.add(ObservationLink.FLD_TYPE, Query.EQUALS, type.name());
		qbe.orderBy(true, ObservationLink.FLD_LASTUPDATE);
		
		List<ObservationLink> observationLinks = qbe.execute();
		List<IObservation> iObservations = new ArrayList<>();
		for (ObservationLink link : observationLinks) {
			String id = link.get(ObservationLink.FLD_TARGETID);
			iObservations.add(Observation.load(id));
		}
		return iObservations;
	}
	
	@Override
	public void addTargetObservation(IObservation target, ObservationLinkType type){
		if (target != null && target.getId() != null && getId() != null) {
			ObservationLink observationLink = new ObservationLink();
			observationLink.set(ObservationLink.FLD_TARGETID, target.getId());
			observationLink.set(ObservationLink.FLD_SOURCEID, getId());
			observationLink.set(ObservationLink.FLD_TYPE, type.name());
		}
	}
	
	@Override
	public Optional<LocalDateTime> getEffectiveTime(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEffectiveTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public void setEffectiveTime(LocalDateTime time){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEffectiveTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}
	
	@Override
	public ObservationCategory getCategory(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCategory((DomainResource) resource.get());
		}
		return ObservationCategory.UNKNOWN;
	}
	
	@Override
	public void setCategory(ObservationCategory category){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCategory((DomainResource) resource.get(), category);
			saveResource(resource.get());
		}
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
	
	
	@Override
	public void setNumericValue(BigDecimal bigDecimal, String unit){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setNumericValue((DomainResource) resource.get(), bigDecimal, unit);
			saveResource(resource.get());
		}
	}
	
	@Override
	public Optional<BigDecimal> getNumericValue(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getNumericValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<String> getNumericValueUnit(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getNumericValueUnit((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	

	@Override
	public List<ObservationComponent> getComponents(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getComponents((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}
	
	@Override
	public void addComponent(ObservationComponent component){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.addComponent((DomainResource) resource.get(), component);
			saveResource(resource.get());
		}
	}
	
	@Override
	public void updateComponent(ObservationComponent component){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.updateComponent((DomainResource) resource.get(), component);
			saveResource(resource.get());
		}
		
	}
	
	@Override
	public void setStringValue(String value){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStringValue((DomainResource) resource.get(), value);
			saveResource(resource.get());
		}
		
	}
	
	@Override
	public Optional<String> getStringValue(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStringValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public void setObservationType(ObservationType observationType){
		if (observationType != null) {
			set(FLD_TYPE, observationType.name());
		}
	}
	
	@Override
	public ObservationType getObservationType(){
		String type = get(FLD_TYPE);
		return type != null ? ObservationType.valueOf(type) : null;
	}
	
	@Override
	public boolean isReferenced(){
		return "1".equals(get(FLD_REFERENCED));
	}
	
	@Override
	public void setReferenced(boolean referenced){
		set(FLD_REFERENCED, referenced ? "1" : "0");
	}
	
	@Override
	public void setComment(String comment){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setComment((DomainResource) resource.get(), comment);
			saveResource(resource.get());
		}
		
	}
	
	@Override
	public Optional<String> getComment(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getComment((DomainResource) resource.get());
		}
		return Optional.empty();
	}
}
