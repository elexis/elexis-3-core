package ch.elexis.core.ui.dbcheck.model;

public class TableARTIKEL extends TableDescriptor {
	
	@Override
	public String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "EAN", "SubID", "LieferantID", "Klasse", "Name",
			"Name_intern", "Maxbestand", "Minbestand", "Istbestand", "EK_Preis", "VK_Preis", "Typ",
			"Codeclass", "ExtID", "LastImport", "ValidFrom", "ValidTo", "ExtInfo"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(15)", "varchar(20)", "varchar(25)",
			"varchar(80)", "varchar(127)", "varchar(127)", "char(4)", "char(4)", "char(4)",
			"char(8)", "char(8)", "varchar(15)", "varchar(10)", "varchar(25)", "char(8)",
			"char(8)", "char(8)", "longblob"
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
	public String[] returnFieldsIn1815(){
		return new String[] {
			"ID", "lastupdate", "deleted", "EAN", "SubID", "LieferantID", "Klasse", "Name",
			"Name_intern", "Maxbestand", "Minbestand", "Istbestand", "EK_Preis", "VK_Preis", "Typ",
			"Codeclass", "ExtID", "LastImport", "ValidFrom", "ValidTo", "ExtInfo", "ATC_code"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn1815(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(15)", "varchar(20)", "varchar(25)",
			"varchar(80)", "varchar(127)", "varchar(127)", "char(4)", "char(4)", "char(4)",
			"char(8)", "char(8)", "varchar(15)", "varchar(10)", "varchar(25)", "char(8)",
			"char(8)", "char(8)", "longblob", "varchar(255)"
		};
	}
}
