package ch.elexis.core.ui.dbcheck.model;

public class TableLABORWERTE extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "PatientID", "datum", "zeit", "ItemID", "Resultat",
			"Flags", "Origin", "Kommentar"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "char(8)", "char(6)",
			"varchar(25)", "varchar(80)", "varchar(10)", "varchar(30)", "longtext"
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
		return new String[] {
			"ItemId does not have an associated entry in LABORITEM or is NULL",
			"SELECT * FROM LABORWERTE l WHERE l.ItemId NOT IN (SELECT id FROM LABORITEMS) OR l.ItemId IS NULL;"
		};
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
	protected String[] returnFieldsIn190(){
		return new String[] {
			"ID", "lastupdate", "deleted", "PatientID", "datum", "zeit", "ItemID", "resultat",
			"Flags", "Origin", "Kommentar", "ExtInfo", "unit", "analysetime", "observationtime",
			"transmissiontime", "refmale", "reffemale", "OriginID"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn190(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "char(8)", "char(6)",
			"varchar(25)", "varchar(255)", "varchar(10)", "varchar(30)", "longtext", "longblob",
			"varchar(255)", "varchar(24)", "varchar(24)", "varchar(24)", "varchar(255)",
			"varchar(255)", "varchar(25)"
		};
	}
}
