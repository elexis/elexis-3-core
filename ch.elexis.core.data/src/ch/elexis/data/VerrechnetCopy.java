package ch.elexis.data;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.VersionInfo;

public class VerrechnetCopy extends Verrechnet {
	
	public static final String REFERENCEID = "ReferenceId"; //$NON-NLS-1$
	
	public static final String VERSIONID = "VERSION"; //$NON-NLS-1$
	private static final String TABLENAME = "VERRECHNETCOPY"; //$NON-NLS-1$
	public static final String VERSION = "1.0.0"; //$NON-NLS-1$
	
	// @formatter:off
	static final String create = 
			"CREATE TABLE " + TABLENAME + " (" + //$NON-NLS-1$ //$NON-NLS-2$
			"ID VARCHAR(25) primary key, " + //$NON-NLS-1$
			"lastupdate BIGINT," + //$NON-NLS-1$
			"deleted CHAR(1) default '0'," + //$NON-NLS-1$
			
			"ReferenceId VARCHAR(25), " + //$NON-NLS-1$
			"Leistg_txt VARCHAR(255)," + //$NON-NLS-1$
			"Leistg_code VARCHAR(25)," + //$NON-NLS-1$
			"Klasse VARCHAR(80)," + //$NON-NLS-1$
			"Zahl CHAR(3)," + //$NON-NLS-1$
			"EK_Kosten CHAR(8)," + //$NON-NLS-1$
			"VK_TP CHAR(8)," + //$NON-NLS-1$
			"VK_Scale CHAR(8)," + //$NON-NLS-1$
			"VK_Preis CHAR(8)," + //$NON-NLS-1$
			"Scale CHAR(4) DEFAULT '100'," + //$NON-NLS-1$
			"Scale2 CHAR(4) DEFAULT '100'," + //$NON-NLS-1$
			"userID VARCHAR(25)," + //$NON-NLS-1$
			"Detail BLOB" + //$NON-NLS-1$		
			");" + //$NON-NLS-1$
			"CREATE INDEX verrechnetcopy1 ON " + TABLENAME + " (" + REFERENCEID + ");" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"INSERT INTO " + TABLENAME + " (ID," + REFERENCEID + ") VALUES (" + JdbcLink.wrap(VERSIONID) + "," + JdbcLink.wrap(VERSION) + ");"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	// @formatter:on
	
	static {
		addMapping(TABLENAME, REFERENCEID, LEISTG_TXT, LEISTG_CODE, CLASS, COUNT, COST_BUYING,
			SCALE_TP_SELLING, SCALE_SELLING, PRICE_SELLING, SCALE, SCALE2, "ExtInfo=Detail", USERID);
		
		if (!tableExists(TABLENAME)) {
			createOrModifyTable(create);
		} else {
			VerrechnetCopy version = load(VERSIONID);
			VersionInfo vi = new VersionInfo(version.get(REFERENCEID));
			if (vi.isOlder(VERSION)) {
				// put update code here when needed
			}
		}
	}
	
	public VerrechnetCopy(String id){
		super(id);
	}
	
	public static VerrechnetCopy load(final String id){
		return new VerrechnetCopy(id);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public VerrechnetCopy(Verrechnet verrechnet, PersistentObject reference){
		create(null);
		set(new String[] {
			REFERENCEID, LEISTG_TXT, LEISTG_CODE, CLASS, COUNT, COST_BUYING, SCALE_TP_SELLING,
			SCALE_SELLING, PRICE_SELLING, SCALE, SCALE2, USERID
		},
			new String[] {
				reference.getId(), verrechnet.get(LEISTG_TXT), verrechnet.get(LEISTG_CODE),
				verrechnet.get(CLASS), verrechnet.get(COUNT), verrechnet.get(COST_BUYING),
				verrechnet.get(SCALE_TP_SELLING), verrechnet.get(SCALE_SELLING),
				verrechnet.get(PRICE_SELLING), verrechnet.get(SCALE), verrechnet.get(SCALE2),
				verrechnet.get(USERID)
			});
	}
}
