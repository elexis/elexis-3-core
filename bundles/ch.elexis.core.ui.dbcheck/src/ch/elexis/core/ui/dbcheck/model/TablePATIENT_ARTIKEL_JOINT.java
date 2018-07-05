package ch.elexis.core.ui.dbcheck.model;

public class TablePATIENT_ARTIKEL_JOINT extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "PatientID", "ArtikelID", "Artikel", "RezeptID",
			"DateFrom", "DateUntil", "Dosis", "Anzahl", "Bemerkung", "ExtInfo"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(80)",
			"varchar(25)", "char(8)", "char(8)", "varchar(10)", "char(3)", "varchar(80)",
			"longblob"
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
		return returnFieldsIn186();
	}
	
	@Override
	protected String[] returnFieldTypesIn1811(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(80)",
			"varchar(25)", "char(8)", "char(8)", "varchar(255)", "char(3)", "varchar(255)",
			"longblob"
		};
	}
	
	@Override
	protected String[] returnFieldsIn300(){
		return new String[] {
			"ID", "lastupdate", "deleted", "PatientID", "ArtikelID", "Artikel", "RezeptID",
			"DateFrom", "DateUntil", "Dosis", "Anzahl", "Bemerkung", "ExtInfo"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn300(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(255)",
			"varchar(25)", "char(8)", "char(8)", "varchar(255)", "char(3)", "varchar(255)",
			"longblob"
		};
	}
	
	@Override
	protected String[] returnFieldsIn310(){
		return new String[] {
			"ID", "lastupdate", "deleted", "PatientID", "ArtikelID", "Artikel", "RezeptID",
			"DateFrom", "DateUntil", "Dosis", "Anzahl", "Bemerkung", "ExtInfo", "prescType",
			"sortOrder", "prescDate", "prescriptor"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn310(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(255)",
			"varchar(25)", "char(8)", "char(8)", "varchar(255)", "char(3)", "varchar(255)",
			"longblob", "char(2)", "char(3)", "char(8)", "varchar(25)"
		};
	}
}
