package ch.elexis.core.findings.fhir.po.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.fhir.po.service.internal.EnumMapping;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Xid;
import ch.rgw.tools.VersionInfo;

public class Condition extends AbstractFhirPersistentObject implements ICondition {
	
	private EnumMapping categoryMapping =
		new EnumMapping(org.hl7.fhir.instance.model.valuesets.ConditionCategory.class,
			ch.elexis.core.findings.ICondition.ConditionCategory.class);
	private EnumMapping statusMapping = new EnumMapping(ConditionClinicalStatus.class,
		ch.elexis.core.findings.ICondition.ConditionStatus.class);
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_CONDITION";
	protected static final String VERSION = "1.0.0";
	
	public static final String FLD_PATIENTID = "patientid"; //$NON-NLS-1$
	public static final String XID_CONDITION_UPDATED = "www.elexis.info/condition/updated";
	
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
		
		Xid.localRegisterXIDDomainIfNotExists(XID_CONDITION_UPDATED, XID_CONDITION_UPDATED,
			Xid.ASSIGNMENT_LOCAL);
		
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
		org.hl7.fhir.dstu3.model.Condition fhirCondition = new org.hl7.fhir.dstu3.model.Condition();
		saveResource(fhirCondition);
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
	public Optional<LocalDate> getDateRecorded(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				(org.hl7.fhir.dstu3.model.Condition) resource.get();
			Date date = fhirCondition.getDateRecorded();
			if (date != null) {
				return Optional.of(getLocalDate(date));
			}
		}
		return Optional.empty();
	}
	
	@Override
	public void setDateRecorded(LocalDate date){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				(org.hl7.fhir.dstu3.model.Condition) resource.get();
			fhirCondition.setDateRecorded(getDate(date));
			saveResource(resource.get());
		}
	}
	
	@Override
	public ConditionCategory getCategory(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				(org.hl7.fhir.dstu3.model.Condition) resource.get();
			List<Coding> coding = fhirCondition.getCategory().getCoding();
			if (!coding.isEmpty()) {
				for (Coding categoryCoding : coding) {
					if (categoryCoding.getSystem()
						.equals("http://hl7.org/fhir/condition-category")) {
						return (ConditionCategory) categoryMapping
							.getLocalEnumValueByCode(categoryCoding.getCode().toUpperCase());
					}
				}
			}
		}
		return ConditionCategory.UNKNOWN;
	}
	
	@Override
	public void setCategory(ConditionCategory category){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				(org.hl7.fhir.dstu3.model.Condition) resource.get();
			CodeableConcept categoryCode = new CodeableConcept();
			org.hl7.fhir.instance.model.valuesets.ConditionCategory fhirCategoryCode =
				(org.hl7.fhir.instance.model.valuesets.ConditionCategory) categoryMapping
					.getFhirEnumValueByEnum(category);
			if (fhirCategoryCode != null) {
				categoryCode
					.setCoding(Collections.singletonList(new Coding(fhirCategoryCode.getSystem(),
						fhirCategoryCode.toCode(), fhirCategoryCode.getDisplay())));
				fhirCondition.setCategory(categoryCode);
			}
			saveResource(resource.get());
		}
	}
	
	@Override
	public ConditionStatus getStatus(){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				(org.hl7.fhir.dstu3.model.Condition) resource.get();
			ConditionClinicalStatus fhirStatus = fhirCondition.getClinicalStatus();
			if (fhirStatus != null) {
				return (ConditionStatus) statusMapping.getLocalEnumValueByCode(fhirStatus.name());
			}
		}
		return ConditionStatus.UNKNOWN;
	}
	
	@Override
	public void setStatus(ConditionStatus status){
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				(org.hl7.fhir.dstu3.model.Condition) resource.get();
			ConditionClinicalStatus fhirCategoryCode =
				(ConditionClinicalStatus) statusMapping.getFhirEnumValueByEnum(status);
			if (fhirCategoryCode != null) {
				fhirCondition.setClinicalStatus(fhirCategoryCode);
			}
			saveResource(resource.get());
		}
	}
}
