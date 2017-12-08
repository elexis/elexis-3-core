package ch.elexis.data.po;

import ch.elexis.data.PersistentObject;

public class OtherListPersistentObject extends PersistentObject {
	private static final String TABLENAME = "OTHERLIST";
	
	public static final String FLD_PERSISTENT_OBJECT = "PersistentObjectId";
	
	private static final String create =
		"CREATE TABLE " + TABLENAME + " (ID VARCHAR(25) primary key, lastupdate BIGINT,"
			+ "deleted CHAR(1) default '0', " + FLD_PERSISTENT_OBJECT + " VARCHAR(25));";
	
	static {
		addMapping(TABLENAME);
		
		if (!tableExists(TABLENAME)) {
			createOrModifyTable(create);
		}
	}
	
	public OtherListPersistentObject(boolean create){
		if (create) {
			create(null);
		}
	}
	
	public OtherListPersistentObject(){
		this(true);
	}
	
	@Override
	public String getLabel(){
		return this.toString();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
}
