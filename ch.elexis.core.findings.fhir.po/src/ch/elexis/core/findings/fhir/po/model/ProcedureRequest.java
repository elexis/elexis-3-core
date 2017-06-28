package ch.elexis.core.findings.fhir.po.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.VersionInfo;

public class ProcedureRequest extends AbstractFhirPersistentObject implements IProcedureRequest {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_PROCEDUREREQUEST";
	protected static final String VERSION = "1.0.0";
	
	public static final String FLD_PATIENTID = "patientid"; //$NON-NLS-1$
	public static final String FLD_ENCOUNTERID = "encounterid"; //$NON-NLS-1$
	public static final String FLD_CONTENT = "content"; //$NON-NLS-1$
	
	//@formatter:off
	protected static final String createDB =
	"CREATE TABLE " + TABLENAME + "(" +
	"ID					VARCHAR(25) PRIMARY KEY," +
	"lastupdate 		BIGINT," +
	"deleted			CHAR(1) default '0'," + 
	"patientid	        VARCHAR(80)," +
	"encounterid	    VARCHAR(80)," +
	"content      		TEXT" + ");" + 
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_PROCEDUREREQUEST_IDX1 ON " + TABLENAME + " (patientid);" +
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_PROCEDUREREQUEST_IDX2 ON " + TABLENAME + " (encounterid);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_PATIENTID + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_PATIENTID, FLD_ENCOUNTERID, FLD_CONTENT);
		
		ProcedureRequest version = load("VERSION");
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
	
	public static ProcedureRequest load(final String id){
		return new ProcedureRequest(id);
	}
	
	protected ProcedureRequest(final String id){
		super(id);
	}
	
	public ProcedureRequest(){}
	
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
	public Optional<LocalDateTime> getScheduledTime(){
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	
	@Override
	public void setScheduledTime(LocalDateTime time){
		// TODO Auto-generated method stub
		
	}
}
