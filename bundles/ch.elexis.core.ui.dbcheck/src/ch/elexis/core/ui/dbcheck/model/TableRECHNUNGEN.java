package ch.elexis.core.ui.dbcheck.model;

public class TableRECHNUNGEN extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "RnNummer", "FallID", "MandantID", "RnDatum",
			"RnStatus", "RnDatumVon", "RnDatumBis", "Betrag", "StatusDatum", "ExtInfo"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(8)", "varchar(25)", "varchar(25)",
			"char(8)", "varchar(20)", "char(8)", "char(8)", "char(8)", "char(8)", "longblob"
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
			"FallID does not have an associated entry in FAELLE or is NULL",
			"SELECT * FROM RECHNUNGEN r WHERE r.FallID NOT IN (SELECT id FROM FAELLE) OR r.FallID IS NULL;",
			"MandantID does not have an associated entry in KONTAKT or is NULL",
			"SELECT * FROM RECHNUNGEN r WHERE r.MandantID NOT IN (SELECT id FROM KONTAKT) OR r.MandantID IS NULL;"
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
