package ch.elexis.core.ui.dbcheck.model;

public class TableLABORITEMS extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "kuerzel", "titel", "laborID", "RefMann", "RefFrauOrTx",
			"Einheit", "typ", "Gruppe", "prio", "billingcode"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(80)", "varchar(80)", "varchar(25)",
			"varchar(80)", "longtext", "varchar(20)", "char(1)", "varchar(25)", "char(3)",
			"varchar(20)"
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
	
	@Override
	protected String[] returnFieldsIn188(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn188(){
		// TODO Auto-generated method stub
		return null;
	}
	
	// 1.8.9 -- "ALTER TABLE LABORITEMS ADD EXPORT VARCHAR(100);"
	@Override
	protected String[] returnFieldsIn189(){
		return new String[] {
			"ID", "lastupdate", "deleted", "kuerzel", "titel", "laborID", "RefMann", "RefFrauOrTx",
			"Einheit", "typ", "Gruppe", "prio", "billingcode", "export"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn189(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(80)", "varchar(80)", "varchar(25)",
			"varchar(80)", "longtext", "varchar(20)", "char(1)", "varchar(25)", "char(3)",
			"varchar(20)", "varchar(100)"
		};
	}
	
	@Override
	protected String[] returnFieldsIn1810(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn1810(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(80)", "varchar(80)", "varchar(25)",
			"varchar(256)", "varchar(256)", "varchar(20)", "char(1)", "varchar(25)", "char(3)",
			"varchar(20)", "varchar(100)"
		};
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
	protected String[] returnFieldsIn190(){
		return new String[] {
			"ID", "lastupdate", "deleted", "kuerzel", "titel", "laborID", "RefMann", "RefFrauOrTx",
			"Einheit", "typ", "Gruppe", "prio", "billingcode", "export", "loinccode", "visible",
			"digits", "formula"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn190(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(80)", "varchar(80)", "varchar(25)",
			"varchar(256)", "varchar(256)", "varchar(20)", "char(1)", "varchar(25)", "char(3)",
			"varchar(128)", "varchar(100)", "varchar(128)", "varchar(1)", "varchar(16)",
			"varchar(255)"
		};
	}
}
