package ch.elexis.data.po;

import ch.elexis.data.PersistentObject;

public class InvalidPersistentObjectImpl extends PersistentObject {
	
	String tablename;
	
	public String getTestGet(){
		return "test";
	}
	
	@Override
	public String getLabel(){
		return null;
	}
	
	@Override
	protected String getTableName(){
		return tablename;
	}
}
