package ch.elexis.data.po;

import ch.elexis.data.PersistentObject;

public class PersistentObjectImpl extends PersistentObject {
	private static final String TABLENAME = "PERSISTENT_OBJECT_IMPL";
	
	public static final String FLD_TEST = "Test";
	public static final String FLD_JOINT_OTHER = "OtherJoint";
	public static final String FLD_LIST_OTHER = "OtherList";
	
	private static final String create =
		"CREATE TABLE " + TABLENAME + " (" + "ID VARCHAR(25) primary key, " + "lastupdate BIGINT,"
			+ "deleted CHAR(1) default '0'," + FLD_TEST + " VARCHAR(255) );";
	private static final String createJoint =
		"CREATE TABLE PO_OTHER_JOINT(ID VARCHAR(25), PersistentObjectId VARCHAR(25),OtherId VARCHAR(25),PRIMARY KEY (PersistentObjectId, OtherId));";
	
	static {
		addMapping(TABLENAME, FLD_TEST,
			FLD_JOINT_OTHER + "=JOINT:PersistentObjectId:OtherId:PO_OTHER_JOINT",
			FLD_LIST_OTHER + "=LIST:PersistentObjectId:OTHERLIST");
		
		initTable();
	}
	
	public static void initTable(){
		if (!tableExists(TABLENAME)) {
			createOrModifyTable(create);
			createOrModifyTable(createJoint);
		}
	}
	
	public PersistentObjectImpl(boolean create){
		if (create) {
			create(null);
		}
	}
	
	public PersistentObjectImpl(){
		this(true);
	}
	
	public String getTestGet(){
		return get(FLD_TEST);
	}
	
	@Override
	public String getLabel(){
		return this.toString();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	/**
	 * @see PersistentObject#getHighestLastUpdate(String)
	 */
	public static long getHighestLastUpdate(){
		return PersistentObject.getHighestLastUpdate(TABLENAME);
	}
}
