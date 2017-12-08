package ch.elexis.core.findings.fhir.po.model;

import ch.elexis.core.findings.IObservationLink;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.VersionInfo;

public class ObservationLink extends PersistentObject implements IObservationLink {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_OBSERVATIONLINK";
	protected static final String VERSION = "1.0.0";
	
	public static final String FLD_SOURCEID = "sourceid"; //$NON-NLS-1$
	public static final String FLD_TARGETID = "targetid"; //$NON-NLS-1$
	public static final String FLD_TYPE = "type"; //$NON-NLS-1$
	public static final String FLD_DESCRIPTION = "description"; //$NON-NLS-1$
	
	//@formatter:off
	protected static final String createDB =
	"CREATE TABLE " + TABLENAME + "(" +
	"ID					VARCHAR(25) PRIMARY KEY," +
	"lastupdate 		BIGINT," +
	"deleted			CHAR(1) default '0'," + 
	"sourceid	        VARCHAR(80)," +
	"targetid	    	VARCHAR(80)," +
	"type	    		CHAR(8)," +
	"description      	VARCHAR(255)" + ");" + 
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATIONLINK_IDX1 ON " + TABLENAME + " (sourceid);" +
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_OBSERVATIONLINK_IDX2 ON " + TABLENAME + " (targetid);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_SOURCEID + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_SOURCEID, FLD_TARGETID, FLD_TYPE, FLD_DESCRIPTION);
		
		ObservationLink version = load("VERSION");
		if (version.state() < PersistentObject.DELETED) {
			createOrModifyTable(createDB);
		} else {
			VersionInfo vi = new VersionInfo(version.get(FLD_SOURCEID));
			if (vi.isOlder(VERSION)) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				version.set(FLD_SOURCEID, VERSION);
			}
		}
	}
	
	public static ObservationLink load(final String id){
		return new ObservationLink(id);
	}
	
	protected ObservationLink(final String id){
		super(id);
	}
	
	public ObservationLink(){
		super();
		create(getId());
	}
	
	@Override
	public String getLabel(){
		return getId();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
}
