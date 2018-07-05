package ch.elexis.core.ui.dbcheck.model;

public class TableKONTAKT extends TableDescriptor {
	
	protected String[] invalidStatesin186(){
		return new String[] {
			"istPerson LIKE \'1\' AND istOrganisation LIKE \'1\'",
			"istPerson LIKE \'0\' AND istPatient LIKE \'1\'",
			"istOrganisation LIKE \'0\' AND istPerson LIKE \'0\' AND istPatient LIKE \'0\' AND istLabor LIKE \'0\' AND istAnwender LIKE \'0\' AND istMandant LIKE \'0\'",
			"Bezeichnung1 LIKE \'\'"
		};
	}
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "istOrganisation", "istPerson", "istPatient",
			"istAnwender", "istMandant", "istLabor", "Land", "Geburtsdatum", "Geschlecht", "Titel",
			"Bezeichnung1", "Bezeichnung2", "Bezeichnung3", "Strasse", "Plz", "Ort", "Telefon1",
			"Telefon2", "Fax", "NatelNr", "EMail", "Website", "Gruppe", "PatientNr", "Anschrift",
			"Bemerkung", "Diagnosen", "PersAnamnese", "SysAnamnese", "FamAnamnese", "Risiken",
			"Allergien", "ExtInfo"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "char(1)", "char(1)", "char(1)", "char(1)",
			"char(1)", "char(1)", "char(3)", "char(8)", "char(1)", "varchar(20)", "varchar(80)",
			"varchar(80)", "varchar(80)", "varchar(80)", "varchar(6)", "varchar(50)",
			"varchar(30)", "varchar(30)", "varchar(30)", "varchar(30)", "varchar(80)",
			"varchar(80)", "varchar(10)", "varchar(40)", "longtext", "longtext", "longblob",
			"longblob", "longblob", "longblob", "longtext", "longtext", "longblob"
		};
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
		return new String[] {
			"ID", "lastupdate", "deleted", "istOrganisation", "istPerson", "istPatient",
			"istAnwender", "istMandant", "istLabor", "Land", "Geburtsdatum", "Geschlecht", "Titel",
			"Bezeichnung1", "Bezeichnung2", "Bezeichnung3", "Strasse", "Plz", "Ort", "Telefon1",
			"Telefon2", "Fax", "NatelNr", "EMail", "Website", "Gruppe", "PatientNr", "Anschrift",
			"Bemerkung", "Diagnosen", "PersAnamnese", "SysAnamnese", "FamAnamnese", "Risiken",
			"Allergien", "ExtInfo", "TitelSuffix"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn1811(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "char(1)", "char(1)", "char(1)", "char(1)",
			"char(1)", "char(1)", "char(3)", "char(8)", "char(1)", "varchar(40)", "varchar(255)",
			"varchar(255)", "varchar(255)", "varchar(255)", "varchar(6)", "varchar(255)",
			"varchar(30)", "varchar(30)", "varchar(30)", "varchar(30)", "varchar(80)",
			"varchar(80)", "varchar(10)", "varchar(40)", "longtext", "longtext", "longblob",
			"longblob", "longblob", "longblob", "longtext", "longtext", "longblob", "varchar(255)"
		};
	}
	
}
