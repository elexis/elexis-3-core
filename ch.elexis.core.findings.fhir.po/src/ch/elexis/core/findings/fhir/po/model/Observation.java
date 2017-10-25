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
import ch.elexis.core.findings.scripting.FindingsScriptingUtil;
import ch.elexis.core.findings.util.fhir.accessor.ObservationAccessor;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.VersionInfo;

public class Observation extends AbstractFhirPersistentObject implements IObservation {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_OBSERVATION";
	protected static final String VERSION = "1.0.3";
	
	public static final String FLD_PATIENTID = "patientid"; //$NON-NLS-1$
	public static final String FLD_ENCOUNTERID = "encounterid"; //$NON-NLS-1$
	public static final String FLD_PERFORMERID = "performerid"; //$NON-NLS-1$
	public static final String FLD_TYPE = "type";
	public static final String FLD_REFERENCED = "referenced";
	public static final String FLD_FORMAT = "format";
	public static final String FLD_SCRIPT = "script";
	public static final String FLD_DECIMALPLACE = "decimalplace";
	public static final String FLD_ORIGINURI = "originuri";
	
	private static final String FORMAT_KEY_VALUE_SPLITTER = ":-:";
	private static final String FORMAT_SPLITTER = ":split:";
	
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
	"originuri			VARCHAR(255)," +
	"decimalplace	    VARCHAR(8)," +
	"format 			TEXT," +
	"script 			TEXT," +
	"content      		TEXT" + ");" + 
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATION_IDX1 ON " + TABLENAME + " (patientid);" +
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATION_IDX2 ON " + TABLENAME + " (encounterid);" +
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATION_IDX3 ON " + TABLENAME + " (originuri);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_PATIENTID + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_PATIENTID, FLD_ENCOUNTERID, FLD_PERFORMERID, FLD_CONTENT,
			FLD_TYPE, FLD_REFERENCED, FLD_FORMAT, FLD_SCRIPT, FLD_DECIMALPLACE, FLD_ORIGINURI);
		
		Observation version = load("VERSION");
		if (version.state() < PersistentObject.DELETED) {
			createOrModifyTable(createDB);
		} else {
			VersionInfo vi = new VersionInfo(version.get(FLD_PATIENTID));
			if (vi.isOlder("1.0.1")) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				createOrModifyTable("ALTER TABLE " + TABLENAME + " ADD " + FLD_TYPE + " CHAR(8);");
				createOrModifyTable("ALTER TABLE " + TABLENAME + " ADD " + FLD_REFERENCED
					+ " CHAR(1) default '0';");
				version.set(FLD_PATIENTID, "1.0.1");
			}
			if (vi.isOlder("1.0.2")) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				createOrModifyTable("ALTER TABLE " + TABLENAME + " ADD " + FLD_FORMAT + " TEXT;");
				version.set(FLD_PATIENTID, "1.0.2");
			}
			if (vi.isOlder(VERSION)) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				createOrModifyTable(
					"ALTER TABLE " + TABLENAME + " ADD " + FLD_DECIMALPLACE + " VARCHAR(8);");
				createOrModifyTable("ALTER TABLE " + TABLENAME + " ADD " + FLD_SCRIPT + " TEXT;");
				createOrModifyTable(
					"ALTER TABLE " + TABLENAME + " ADD " + FLD_ORIGINURI + " VARCHAR(255);");
				createOrModifyTable("CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATION_IDX3 ON "
					+ TABLENAME + " (originuri);");
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
		for (ObservationLink link : observationLinks) {
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
			if (FindingsScriptingUtil.hasScript(this)) {
				FindingsScriptingUtil.evaluate(this);
			}
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
	
	@Override
	public void addFormat(String key, String value){
		StringBuilder builder = new StringBuilder(get(FLD_FORMAT));
		String dbValue = getFormat(key);
		String dbKeyValue = key + FORMAT_KEY_VALUE_SPLITTER + dbValue;
		
		int idx = builder.indexOf(dbKeyValue);
		if (idx == -1) {
			if (builder.length() > 0) {
				builder.append(FORMAT_SPLITTER);
			}
			builder.append(key + FORMAT_KEY_VALUE_SPLITTER + value);
		} else {
			builder.replace(idx, idx + dbKeyValue.length(),
				key + FORMAT_KEY_VALUE_SPLITTER + value);
		}
		set(FLD_FORMAT, builder.toString());
	}
	
	@Override
	public String getFormat(String key){
		String format = checkNull(get(FLD_FORMAT));
		if (format.contains(key + FORMAT_KEY_VALUE_SPLITTER)) {
			String[] splits = format.split(key + FORMAT_KEY_VALUE_SPLITTER);
			if (splits.length > 1) {
				return splits[1].split(FORMAT_SPLITTER)[0];
			}
		}
		return "";
	}
	
	@Override
	public Optional<String> getScript(){
		String value = get(FLD_SCRIPT);
		if (value != null && !value.isEmpty()) {
			return Optional.of(value);
		}
		return Optional.empty();
	}
	
	@Override
	public void setScript(String script){
		set(FLD_SCRIPT, script);
	}
	
	@Override
	public int getDecimalPlace(){
		String value = get(FLD_DECIMALPLACE);
		if (value != null && !value.isEmpty()) {
			return Integer.valueOf(value);
		}
		return -1;
	}
	
	@Override
	public void setDecimalPlace(int value){
		set(FLD_DECIMALPLACE, Integer.toString(value));
	}
	
	@Override
	public Optional<String> getOriginUri(){
		String value = get(FLD_ORIGINURI);
		if (value != null && !value.isEmpty()) {
			return Optional.of(value);
		}
		return Optional.empty();
	}
	
	@Override
	public void setOriginUri(String uri){
		set(FLD_ORIGINURI, uri);
	}
}
