package ch.rgw.tools;

import java.io.UnsupportedEncodingException;

public class JdbcLinkUtil {

	public final static String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	public final static String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";
	public final static String H2_DRIVER_CLASS_NAME = "org.h2.Driver";

	public static final String DBFLAVOR_MYSQL = "mysql";
	public static final String DBFLAVOR_POSTGRESQL = "postgresql";
	public static final String DBFLAVOR_H2 = "h2";

	/**
	 * Utility-Funktion zum Einpacken von Strings in Hochkommata und escapen
	 * illegaler Zeichen
	 *
	 * @param s der String
	 * @return Datenbankkonform eingepackte String
	 *
	 * @deprecated only escapes for DBFLAVOR_MYSQL, use
	 *             {@link JdbcLink#wrapFlavored(String)} for correct wrapping
	 */
	public static String wrap(String s) {
		if (StringTool.isNothing(s)) {
			return "''";
		}
		try {
			return wrap(s.getBytes("UTF-8"), DBFLAVOR_MYSQL);
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			return wrap(s.getBytes(), DBFLAVOR_MYSQL);
		}
	}

	/**
	 * Utility-Funktion zum Datenbankkonformen Verpacken von byte arrays zwecks
	 * Einfügen in BLOB-Felder.
	 *
	 * @param flavor TODO
	 * @param b      das rohe byte array
	 * @return das verpackte array in Form eines String
	 */
	public static String wrap(byte[] in, String flavor) {

		byte[] out = new byte[2 * in.length + 2];
		int j = 0;
		out[j++] = '\'';
		for (int i = 0; i < in.length; i++) {
			switch (in[i]) {
			case 0:
			case 34:

			case '\'':
				if (flavor.startsWith(DBFLAVOR_POSTGRESQL) || flavor.startsWith("hsql")) {
					out[j++] = '\'';
					break;
				} else if (flavor.startsWith(DBFLAVOR_H2)) {
					out[j++] = 39;
					break;
				}
			case 92:
				boolean before = (i > 1 && in[i - 1] == 92);
				boolean after = (i < in.length - 1 && in[i + 1] == 92);
				if (!before && !after) {
					out[j++] = '\\';
				}
			}
			out[j++] = in[i];
		}
		out[j++] = '\'';
		try {
			return new String(out, 0, j, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			return null;
		}
	}

}
