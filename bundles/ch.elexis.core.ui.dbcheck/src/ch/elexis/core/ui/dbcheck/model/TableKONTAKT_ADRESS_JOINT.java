package ch.elexis.core.ui.dbcheck.model;

public class TableKONTAKT_ADRESS_JOINT extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "myID", "otherID", "Bezug"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(30)"
		};
	}
	
	@Override
	protected String[] invalidStatesin186(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] cleaningSQLforPostgreSQLin186(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] cleaningSQLforMySQLin186(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] referentialIntegrityCheckSQLin186(){
		// TODO Auto-generated method stub
		return null;
	}
	
	// 1.8.8 -- "ALTER TABLE KONTAKT_ADRESS_JOINT MODIFY Bezug VARCHAR(80);"
	@Override
	protected String[] returnFieldsIn188(){
		return new String[] {
			"ID", "lastupdate", "deleted", "myID", "otherID", "Bezug"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn188(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(80)"
		};
	}
	
	@Override
	protected String[] returnFieldsIn189(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn189(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldsIn1810(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn1810(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldsIn1811(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn1811(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldsIn300(){
		return new String[] {
			"ID", "lastupdate", "deleted", "myID", "otherID", "Bezug", "myRType", "otherRType"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn300(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(80)",
			"char(4)", "char(4)"
		};
	}
}
