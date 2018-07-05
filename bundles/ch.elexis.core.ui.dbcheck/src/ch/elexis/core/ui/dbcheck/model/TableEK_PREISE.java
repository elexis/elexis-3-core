package ch.elexis.core.ui.dbcheck.model;

public class TableEK_PREISE extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"TYP", "ID", "lastupdate", "DATUM_VON", "DATUM_BIS", "MULTIPLIKATOR"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(80)", "varchar(25)", "bigint(20)", "char(8)", "char(8)", "char(4)"
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
	protected String[] returnFieldTypesIn1812(){
		return new String[] {
			"varchar(80)", "varchar(25)", "bigint(20)", "char(8)", "char(8)", "char(8)"
		};
	}
	
}
