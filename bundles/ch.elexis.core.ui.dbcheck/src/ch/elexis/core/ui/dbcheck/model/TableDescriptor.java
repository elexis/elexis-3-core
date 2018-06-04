package ch.elexis.core.ui.dbcheck.model;

/**
 * Table descriptor of Elexis database model > 1.8.6 see {@link ch.elexis.util.DBUpdate} for current
 * version
 * 
 * @author Marco Descher <descher@medevit.at>
 */
public abstract class TableDescriptor {
	
	protected abstract String[] returnFieldsIn186();
	
	protected abstract String[] returnFieldTypesIn186();
	
	protected abstract String[] invalidStatesin186();
	
	protected abstract String[] cleaningSQLforPostgreSQLin186();
	
	protected abstract String[] cleaningSQLforMySQLin186();
	
	protected abstract String[] referentialIntegrityCheckSQLin186();
	
	// 1.8.7 -- "ALTER TABLE LOGS MODIFY station VARCHAR(40);" --> ident zu 1.8.6
	// 1.8.8 -- "ALTER TABLE KONTAKT_ADRESS_JOINT MODIFY Bezug VARCHAR(80);"
	protected abstract String[] returnFieldsIn188();
	
	protected abstract String[] returnFieldTypesIn188();
	
	// 1.8.9 -- "ALTER TABLE LABORITEMS ADD EXPORT VARCHAR(100);"
	protected abstract String[] returnFieldsIn189();
	
	protected abstract String[] returnFieldTypesIn189();
	
	// 1.8.10 -- "ALTER TABLE LABORITEMS MODIFY RefFrauOrTx VARCHAR(256);" +
	// 1.8.10 -- "ALTER TABLE LABORITEMS MODIFY RefMann     VARCHAR(256);"
	protected abstract String[] returnFieldsIn1810();
	
	protected abstract String[] returnFieldTypesIn1810();
	
	// 1.8.11 -- "ALTER TABLE KONTAKT MODIFY Titel VARCHAR(40);" +
	// 1.8.11 -- "ALTER TABLE KONTAKT ADD TitelSuffix VARCHAR(40);"
	protected abstract String[] returnFieldsIn1811();
	
	protected abstract String[] returnFieldTypesIn1811();
	
	// 1.8.12 -- "ALTER TABLE VK_PREISE MODIFY MULTIPLIKATOR VARCHAR(8); +
	// 1.8.12 -- "ALTER TABLE EK_PREISE MODIFY MULTIPLIKATOR VARCHAR(8);"
	protected String[] returnFieldTypesIn1812(){
		// Default behavior, is overridden in TableEK_PREISE and TableVK_PREISE
		return getFieldTypes(DBModel.VERSION1811);
	}
	
	// 1.8.13 -- "ALTER TABLE LEISTUNGEN MODIFY VK_TP VARCHAR(8);"
	// 1.8.13 -- "ALTER TABLE LEISTUNGEN MODIFY VK_SCALE VARCHAR(8);"
	protected String[] returnFieldTypesIn1813(){
		// Default behavior, is overridden in LEISTUNGEN
		return getFieldTypes(DBModel.VERSION1811);
	}
	
	// 1.8.14 -- "ALTER TABLE LOGS MODIFY OID VARCHAR(255);"
	// 1.8.14 -- "ALTER TABLE LOGS MODIFY station VARCHAR(255);"
	protected String[] returnFieldTypesIn1814(){
		// Default behavior, is overridden in LOGS
		return getFieldTypes(DBModel.VERSION1811);
	}
	
	// 1.8.15 -- "ALTER TABLE ARTIKEL ADD ATC_code VARCHAR(255);"
	protected String[] returnFieldsIn1815(){
		// Default behavior, is overridden in ARTIKEL
		return getFields(DBModel.VERSION1811);
	}
	
	protected String[] returnFieldTypesIn1815(){
		// Default behavior, is overridden in ARTIKEL
		return getFieldTypes(DBModel.VERSION1811);
	}
	
	// 1.8.16 -- "ALTER TABLE LEISTUNGEN ADD userID VARCHAR(25);"
	protected String[] returnFieldsIn1816(){
		// Default behavior, is overridden in LEISTUNGEN
		return getFields(DBModel.VERSION1815);
	}
	
	protected String[] returnFieldTypesIn1816(){
		// Default behavior, is overridden in LEISTUNGEN
		return getFieldTypes(DBModel.VERSION1815);
	}
	
	// 1.9.0 -- LABORITEMS and LABORWERTE
	protected String[] returnFieldsIn190(){
		// Default behavior, is overriden in LABORITEMS and LABORWERTE
		return getFields(DBModel.VERSION1816);
	}
	
	protected String[] returnFieldTypesIn190(){
		// Default behavior, is overriden in LABORITEMS and LABORWERTE
		return getFieldTypes(DBModel.VERSION1816);
	}
	
	protected String[] returnFieldsIn300(){
		return getFields(DBModel.VERSION190);
	}
	
	protected String[] returnFieldTypesIn300(){
		return getFieldTypes(DBModel.VERSION190);
	}
	
	protected String[] returnFieldsIn310(){
		return getFields(DBModel.VERSION190);
	}
	
	protected String[] returnFieldTypesIn310(){
		return getFieldTypes(DBModel.VERSION190);
	}
	
	/**
	 * Die definierten Feldnamen einer Tabelle in einer spezifischen Version
	 * 
	 * @param version
	 * @return
	 */
	public String[] getFields(String version){
		if (version.equalsIgnoreCase(DBModel.VERSION186))
			return returnFieldsIn186();
		if (version.equalsIgnoreCase(DBModel.VERSION187))
			return returnFieldsIn186();
		if (version.equalsIgnoreCase(DBModel.VERSION188)) {
			if (returnFieldsIn188() != null)
				return returnFieldsIn188();
			return returnFieldsIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION189)) {
			if (returnFieldsIn189() != null)
				return returnFieldsIn189();
			if (returnFieldsIn188() != null)
				return returnFieldsIn188();
			return returnFieldsIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1810)) {
			if (returnFieldsIn1810() != null)
				return returnFieldsIn1810();
			if (returnFieldsIn189() != null)
				return returnFieldsIn189();
			if (returnFieldsIn188() != null)
				return returnFieldsIn188();
			return returnFieldsIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1811)
			|| version.equalsIgnoreCase(DBModel.VERSION1812)
			|| version.equalsIgnoreCase(DBModel.VERSION1813)
			|| version.equalsIgnoreCase(DBModel.VERSION1814)) {
			if (returnFieldsIn1811() != null)
				return returnFieldsIn1811();
			if (returnFieldsIn1810() != null)
				return returnFieldsIn1810();
			if (returnFieldsIn189() != null)
				return returnFieldsIn189();
			if (returnFieldsIn188() != null)
				return returnFieldsIn188();
			return returnFieldsIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1815)) {
			if (returnFieldsIn1815() != null)
				return returnFieldsIn1815();
			return getFields(DBModel.VERSION1814);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1816)) {
			if (returnFieldsIn1816() != null)
				return returnFieldsIn1816();
			return getFields(DBModel.VERSION1815);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION190)) {
			if (returnFieldsIn190() != null)
				return returnFieldsIn190();
			return getFields(DBModel.VERSION1816);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION300)) {
			if (returnFieldsIn300() != null)
				return returnFieldsIn300();
			return getFields(DBModel.VERSION190);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION310)) {
			if (returnFieldsIn310() != null) {
				return returnFieldsIn310();
			}
			return getFields(DBModel.VERSION300);
		}
		
		throw new IllegalStateException("The correct entry was not returned for " + version);
	}
	
	/**
	 * Die zur getFields(version) Tabelle zugehörigen Datentypen
	 * 
	 * @param version
	 * @return
	 */
	public String[] getFieldTypes(String version){
		if (version.equalsIgnoreCase(DBModel.VERSION186))
			return returnFieldTypesIn186();
		if (version.equalsIgnoreCase(DBModel.VERSION187))
			return returnFieldTypesIn186();
		if (version.equalsIgnoreCase(DBModel.VERSION188)) {
			if (returnFieldTypesIn188() != null)
				return returnFieldTypesIn188();
			return returnFieldTypesIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION189)) {
			if (returnFieldTypesIn189() != null)
				return returnFieldTypesIn189();
			if (returnFieldTypesIn188() != null)
				return returnFieldTypesIn188();
			return returnFieldTypesIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1810)) {
			if (returnFieldTypesIn1810() != null)
				return returnFieldTypesIn1810();
			if (returnFieldTypesIn189() != null)
				return returnFieldTypesIn189();
			if (returnFieldTypesIn188() != null)
				return returnFieldTypesIn188();
			return returnFieldTypesIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1811)) {
			if (returnFieldTypesIn1811() != null)
				return returnFieldTypesIn1811();
			if (returnFieldTypesIn1810() != null)
				return returnFieldTypesIn1810();
			if (returnFieldTypesIn189() != null)
				return returnFieldTypesIn189();
			if (returnFieldTypesIn188() != null)
				return returnFieldTypesIn188();
			return returnFieldTypesIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1812)) {
			if (returnFieldTypesIn1812() != null)
				return returnFieldTypesIn1812();
			if (returnFieldTypesIn1811() != null)
				return returnFieldTypesIn1811();
			if (returnFieldTypesIn1810() != null)
				return returnFieldTypesIn1810();
			if (returnFieldTypesIn189() != null)
				return returnFieldTypesIn189();
			if (returnFieldTypesIn188() != null)
				return returnFieldTypesIn188();
			return returnFieldTypesIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1813)) {
			if (returnFieldTypesIn1813() != null)
				return returnFieldTypesIn1813();
			if (returnFieldTypesIn1812() != null)
				return returnFieldTypesIn1812();
			if (returnFieldTypesIn1811() != null)
				return returnFieldTypesIn1811();
			if (returnFieldTypesIn1810() != null)
				return returnFieldTypesIn1810();
			if (returnFieldTypesIn189() != null)
				return returnFieldTypesIn189();
			if (returnFieldTypesIn188() != null)
				return returnFieldTypesIn188();
			return returnFieldTypesIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1814)) {
			if (returnFieldTypesIn1814() != null)
				return returnFieldTypesIn1814();
			if (returnFieldTypesIn1813() != null)
				return returnFieldTypesIn1813();
			if (returnFieldTypesIn1812() != null)
				return returnFieldTypesIn1812();
			if (returnFieldTypesIn1811() != null)
				return returnFieldTypesIn1811();
			if (returnFieldTypesIn1810() != null)
				return returnFieldTypesIn1810();
			if (returnFieldTypesIn189() != null)
				return returnFieldTypesIn189();
			if (returnFieldTypesIn188() != null)
				return returnFieldTypesIn188();
			return returnFieldTypesIn186();
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1815)) {
			if (returnFieldsIn1815() != null)
				return returnFieldTypesIn1815();
			return getFieldTypes(DBModel.VERSION1814);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION1816)) {
			if (returnFieldsIn1816() != null)
				return returnFieldTypesIn1816();
			return getFieldTypes(DBModel.VERSION1815);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION190)) {
			if (returnFieldsIn190() != null)
				return returnFieldTypesIn190();
			return getFieldTypes(DBModel.VERSION1816);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION300)) {
			if (returnFieldsIn300() != null)
				return returnFieldTypesIn300();
			return getFieldTypes(DBModel.VERSION190);
		}
		if (version.equalsIgnoreCase(DBModel.VERSION310)) {
			if (returnFieldsIn310() != null)
				return returnFieldTypesIn310();
			return getFieldTypes(DBModel.VERSION300);
		}
		
		throw new IllegalStateException(
			"The correct field types entry was not returned for " + version);
	}
	
	/**
	 * Enthält eine Liste von ungültigen Zuständen innerhalb einer bestimmten Tabelle in der Form
	 * e.g.: Bezeichnung1 LIKE '' die umgebenden Query Teile wie SELECT FROM etc. müssen selber
	 * angehängt werden.
	 * 
	 * @param version
	 * @return
	 */
	public String[] getInvalidStates(String version){
		if (version.equalsIgnoreCase(DBModel.VERSION186))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION187))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION188))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION189))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1810))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1811))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1812))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1813))
			return invalidStatesin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1814))
			return invalidStatesin186();
		return null;
	}
	
	/**
	 * Enthält eine Liste von Reinigungsskripts einer Tabelle; der ungerade Eintrag enthält die
	 * Beschreibung, der gerade das SQL Statement
	 * 
	 * @param version
	 * @return
	 */
	public String[] getCleaningSQLforPostgresSQL(String version){
		if (version.equalsIgnoreCase(DBModel.VERSION186))
			return cleaningSQLforPostgreSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION187))
			return cleaningSQLforPostgreSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION188))
			return cleaningSQLforPostgreSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION189))
			return cleaningSQLforPostgreSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1810))
			return cleaningSQLforPostgreSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1811))
			return cleaningSQLforPostgreSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1812))
			return cleaningSQLforPostgreSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1813)
			|| version.equalsIgnoreCase(DBModel.VERSION1814))
			return cleaningSQLforPostgreSQLin186();
		return null;
	}
	
	/**
	 * Enthält Tests zur referentiellen Integrität. Die SQL Skripts müssen so definiert sein dass
	 * Sie im Fehlerfalle eine Ausgabe erzeugen. Der ungerade Eintrag enthält die Beschreibung des
	 * Fehlers, der gerade das SQL Statement auf das geprüft wird, und das bei Erzeugen eines
	 * Resultates dem angegebenen Fehler entspricht.
	 * 
	 * @param version
	 * @return
	 */
	public String[] getReferentialIntegrityCheck(String version){
		if (version.equalsIgnoreCase(DBModel.VERSION186))
			return referentialIntegrityCheckSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION187))
			return referentialIntegrityCheckSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION188))
			return referentialIntegrityCheckSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION189))
			return referentialIntegrityCheckSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1810))
			return referentialIntegrityCheckSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1811))
			return referentialIntegrityCheckSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1812))
			return referentialIntegrityCheckSQLin186();
		if (version.equalsIgnoreCase(DBModel.VERSION1813)
			|| version.equalsIgnoreCase(DBModel.VERSION1814))
			return referentialIntegrityCheckSQLin186();
		return null;
	}
}
