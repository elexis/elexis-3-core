package ch.elexis.core.ui.dbcheck.model;

public class TableLEISTUNGEN extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "BEHANDLUNG", "LEISTG_TXT", "LEISTG_CODE", "KLASSE",
			"ZAHL", "EK_KOSTEN", "VK_TP", "VK_SCALE", "VK_PREIS", "SCALE", "SCALE2", "DETAIL"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(255)", "varchar(25)",
			"varchar(80)", "char(3)", "char(8)", "char(6)", "char(6)", "char(8)", "char(4)",
			"char(4)", "longblob"
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
	protected String[] returnFieldTypesIn1813(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(255)", "varchar(25)",
			"varchar(80)", "char(3)", "char(8)", "char(8)", "char(8)", "char(8)", "char(4)",
			"char(4)", "longblob"
		};
	}
	
	@Override
	protected String[] returnFieldsIn1816(){
		return new String[] {
			"ID", "lastupdate", "deleted", "BEHANDLUNG", "LEISTG_TXT", "LEISTG_CODE", "KLASSE",
			"ZAHL", "EK_KOSTEN", "VK_TP", "VK_SCALE", "VK_PREIS", "SCALE", "SCALE2", "DETAIL",
			"userID"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn1816(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(255)", "varchar(25)",
			"varchar(80)", "char(3)", "char(8)", "char(8)", "char(8)", "char(8)", "char(4)",
			"char(4)", "longblob", "varchar(25)"
		};
	}
}
