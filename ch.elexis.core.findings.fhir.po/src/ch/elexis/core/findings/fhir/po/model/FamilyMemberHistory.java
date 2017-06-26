package ch.elexis.core.findings.fhir.po.model;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.util.fhir.accessor.ConditionAccessor;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.VersionInfo;

public class FamilyMemberHistory extends AbstractFhirPersistentObject
		implements IFamilyMemberHistory {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_FAMILYMEMBERHISTORY";
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
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_FAMILYMEMBERHISTORY_IDX1 ON " + TABLENAME + " (patientid);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_PATIENTID + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_PATIENTID, FLD_CONTENT);
		
		FamilyMemberHistory version = load("VERSION");
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
	
	public static FamilyMemberHistory load(final String id){
		return new FamilyMemberHistory(id);
	}
	
	protected FamilyMemberHistory(final String id){
		super(id);
	}
	
	public FamilyMemberHistory(){
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
	
	
}
