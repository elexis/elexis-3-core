package ch.elexis.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jdt.NonNull;
import ch.rgw.io.Settings;
import ch.rgw.tools.StringTool;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class DBConnectionUtil {

	private static Logger logger = LoggerFactory.getLogger(DBConnectionUtil.class);

	/**
	 * Interface for use with
	 * {@link PersistentObject#foldObject(byte[], IClassResolver)} to map classes on
	 * deserialisation using {@link ObjectInputStream}.
	 *
	 */
	private static interface IClassResolver {
		public Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException;
	}

	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 *
	 * @param flat     the byte array
	 * @param resolver {@link IClassResolver} implementation used for class
	 *                 resolving / mapping
	 * @return the original Hashtable or null if no Hashtable could be created from
	 *         the array
	 */
	private static Object foldObject(final byte[] flat, IClassResolver resolver) {
		if (flat.length == 0) {
			return null;
		}
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(flat))) {
			ZipEntry entry = zis.getNextEntry();
			if (entry != null) {
				try (ObjectInputStream ois = new ObjectInputStream(zis) {
					@Override
					protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass desc)
							throws IOException, ClassNotFoundException {
						if (resolver != null) {
							Class<?> resolved = resolver.resolveClass(desc);
							return (resolved != null) ? resolved : super.resolveClass(desc);
						} else {
							return super.resolveClass(desc);
						}
					};
				}) {
					return ois.readObject();
				}
			} else {
				return null;
			}
		} catch (Exception ex) {
			logger.error("Error unfolding object", ex); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 *
	 * @param flat the byte array
	 *
	 * @return the original Hashtable or null if no Hashtable could be created from
	 *         the array
	 */
	private static Object foldObject(final byte[] flat) {
		return foldObject(flat, null);
	}

	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 *
	 * @param flat the byte array
	 * @return the original Hashtable or null if no Hashtable could be created from
	 *         the array
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable<Object, Object> fold(final byte[] flat) {
		return (Hashtable<Object, Object>) foldObject(flat);
	}

	/**
	 * @since 3.8 due to mysql jdbc update a timezone problem may exist, see e.g.
	 *        https://github.com/elexis/elexis-3-core/issues/273 - we fix this by
	 *        adding this parameter if not yet included
	 */
	private static String applyMySqlTimeZoneWorkaround(String dbConnectString) {
		if (dbConnectString.startsWith("jdbc:mysql:") && !dbConnectString.contains("serverTimezone")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (dbConnectString.contains("?")) { //$NON-NLS-1$
				dbConnectString += "&serverTimezone=Europe/Zurich"; //$NON-NLS-1$
			} else {
				dbConnectString += "?serverTimezone=Europe/Zurich"; //$NON-NLS-1$
			}
			logger.info("MySQL dbConnection string correction [{}]", dbConnectString); //$NON-NLS-1$
		}
		return dbConnectString;
	}

	/**
	 * Get a {@link DBConnection} form various sources. Sources are checked in
	 * following order.<br/>
	 * <li>System Property - RunFromScratch (initializes a fresh h2 database)</li>
	 * <li>System Property - ch.elexis.dbSpec, etc.</li>
	 * <li>Provided Settings</li> <br />
	 *
	 * @param settings
	 * @return
	 */
	public static Optional<DBConnection> getDBConnection(Settings settings) {
		Hashtable<Object, Object> hConn = getConnectionHashtable(settings);
		if (hConn != null) {
			DBConnection ret = new DBConnection();
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING))) {
				String url = (String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING);
				url = applyMySqlTimeZoneWorkaround(url);
				ret.connectionString = url;
				DBConnection.getHostName(url).ifPresent(h -> ret.hostName = h);
				DBConnection.getDatabaseName(url).ifPresent(db -> ret.databaseName = db);
			}
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_USER))) {
				ret.username = (String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_USER);
			}
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_PASS))) {
				ret.password = (String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_PASS);
			}
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_DRIVER))) {
				Optional<DBType> type = DBType
						.valueOfDriver((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_DRIVER));
				type.ifPresent(t -> ret.rdbmsType = t);
			}
			if (ret.allValuesSet()) {
				return Optional.of(ret);
			} else {
				StringBuilder sb = new StringBuilder();
				for (Object object : hConn.keySet()) {
					if (object instanceof String) {
						sb.append(StringUtils.LF).append(object).append("->").append(hConn.get(object)); //$NON-NLS-1$
					}
				}
				logger.error("Could not get a valid DBConnection from connection setting:" + sb.toString()); //$NON-NLS-1$
			}
		}
		return Optional.empty();
	}

	/**
	 *
	 * @return a {@link Hashtable} containing the connection parameters, use
	 *         {@link Preferences#CFG_FOLDED_CONNECTION} to retrieve the required
	 *         parameters, castable to {@link String}
	 */
	public static @NonNull Hashtable<Object, Object> getConnectionHashtable(Settings settings) {
		Hashtable<Object, Object> ret = new Hashtable<>();
		String cnt = settings.get(Preferences.CFG_FOLDED_CONNECTION, null);
		if (cnt != null) {
			ret = fold(StringTool.dePrintable(cnt));
		}
		return ret;
	}

	/**
	 * The default database connection product name
	 * 
	 * @return "H2, "MySQL", "PostgreSQL" or "unknown"
	 * @since 3.10
	 */
	public static String getDatabaseProductName() {
		Optional<DataSource> defaultDataSource = OsgiServiceUtil.getService(DataSource.class, "(id=default)"); //$NON-NLS-1$
		if (defaultDataSource.isPresent()) {
			try {
				Connection connection = defaultDataSource.get().getConnection();
				return connection.getMetaData().getDatabaseProductName();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			OsgiServiceUtil.ungetService(DataSource.class);
		}
		return "unknown"; //$NON-NLS-1$
	}

	/**
	 * Marshall this object into a storable xml
	 *
	 * @param os
	 * @param dbc
	 * @throws JAXBException
	 */
	public static void marshall(OutputStream os, DBConnection dbc) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(DBConnection.class);
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(dbc, os);
	}

	/**
	 * Unmarshall a DBConnection object created by {@link #marshall()}
	 *
	 * @param is
	 * @return
	 * @throws JAXBException
	 */
	public static DBConnection unmarshall(InputStream is) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(DBConnection.class);
		Unmarshaller um = jaxbContext.createUnmarshaller();
		Object o = um.unmarshal(is);
		return (DBConnection) o;
	}

	public String marshallIntoString() {
		try (StringWriter sw = new StringWriter()) {
			JAXBContext jaxbContext = JAXBContext.newInstance(DBConnection.class);
			Marshaller m = jaxbContext.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(this, sw);
			return sw.toString();
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
			return StringUtils.EMPTY;
		}
	}

	public static DBConnection unmarshall(String value) {
		if (value == null) {
			return null;
		}
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					value.getBytes(StandardCharsets.UTF_8));
			return unmarshall(byteArrayInputStream);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DBConnection();
	}

}
