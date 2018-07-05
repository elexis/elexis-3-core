package ch.elexis.core.ui.dbcheck.model;

public class TableFAELLE extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "PatientID", "GarantID", "KostentrID", "VersNummer",
			"FallNummer", "BetriebsNummer", "Diagnosen", "DatumVon", "DatumBis", "Bezeichnung",
			"Grund", "Gesetz", "Status", "ExtInfo"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)", "varchar(25)",
			"varchar(25)", "varchar(25)", "varchar(25)", "varchar(80)", "char(8)", "char(8)",
			"varchar(80)", "varchar(80)", "varchar(20)", "varchar(80)", "longblob"
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
			"PatientID does not have an associated entry in KONTAKTE or is NULL",
			"SELECT * FROM FAELLE f WHERE f.PatientID NOT IN (SELECT id FROM KONTAKT) OR f.PatientID IS NULL;"
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
	
}
