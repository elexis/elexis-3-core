package ch.elexis.data.po;

import ch.elexis.data.PersistentObject;

public class OtherJointPersistentObject extends PersistentObject {
	private static final String TABLENAME = "OTHERJOINT";
	
	private static final String create = "CREATE TABLE " + TABLENAME + " ("
		+ "ID VARCHAR(25) primary key, " + "lastupdate BIGINT," + "deleted CHAR(1) default '0');";
	
	static {
		addMapping(TABLENAME);
		
		initTable();
	}
	
	public static void initTable(){
		if (!tableExists(TABLENAME)) {
			createOrModifyTable(create);
		}
	}
	
	public OtherJointPersistentObject(boolean create){
		if (create) {
			create(null);
		}
	}
	
	public OtherJointPersistentObject(){
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
