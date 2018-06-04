package ch.elexis.core.ui.dbcheck.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBModel {
	
	private static Logger log = LoggerFactory.getLogger(DBModel.class);
	
	public static final String VERSION186 = "1.8.6";
	public static final String VERSION187 = "1.8.7";
	public static final String VERSION188 = "1.8.8";
	public static final String VERSION189 = "1.8.9";
	public static final String VERSION1810 = "1.8.10";
	public static final String VERSION1811 = "1.8.11";
	public static final String VERSION1812 = "1.8.12";
	public static final String VERSION1813 = "1.8.13";
	public static final String VERSION1814 = "1.8.14";
	public static final String VERSION1815 = "1.8.15";
	public static final String VERSION1816 = "1.8.16";
	public static final String VERSION190 = "1.9.0";
	public static final String VERSION300 = "3.0.0";
	public static final String VERSION310 = "3.1.0";
	
	public static String[] tablesInV186 = {
		"ARTIKEL", "ARTIKEL_DETAILS", "AUF", "BBS", "BEHANDLUNGEN", "BEHDL_DG_JOINT", "BRIEFE",
		"CONFIG", "DBIMAGE", "DIAGNOSEN", "EIGENLEISTUNGEN", "EK_PREISE", "HEAP", "KONTAKT",
		"KONTO", "LABORITEMS", "LABGROUPS", "LABORWERTE", "LEISTUNGEN", "LEISTUNGSBLOCK", "LOGS",
		"PATIENT_ARTIKEL_JOINT", "FAELLE", "ETIKETTEN", "ETIKETTEN_OBJCLASS_LINK",
		"KONTAKT_ADRESS_JOINT", "LABGROUP_ITEM_JOINT", "ETIKETTEN_OBJECT_LINK", "USERCONFIG",
		"HEAP2", "TRACES", "OUTPUT_LOG", "VK_PREISE", "RECHNUNGEN", "REMINDERS",
		"REMINDERS_RESPONSIBLE_LINK", "REZEPTE", "XID", "ZAHLUNGEN"
	};
	
	public static String[] getTableModel(String version){
		return tablesInV186;
	}
	
	public static TableDescriptor getTableDescription(String table){
		try {
			Class<?> tableClass =
				Class.forName("ch.elexis.core.ui.dbcheck.model.Table" + table.toUpperCase());
			return (TableDescriptor) tableClass.newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.warn("Could not resolve table "+table, e);
			return null;
		}
	}
}
