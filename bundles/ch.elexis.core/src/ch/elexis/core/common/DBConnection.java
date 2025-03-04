/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import ch.rgw.tools.JdbcLink;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "rdbmsType", "hostName", "port", "databaseName", "connectionString", "username", "password",
		"text" })
public class DBConnection implements Serializable {

	private static final long serialVersionUID = -7571011690246990109L;

	@XmlType
	@XmlEnum(String.class)
	public enum DBType {
		@XmlEnumValue("MYSQL")
		MySQL(JdbcLink.MYSQL_DRIVER_CLASS_NAME, "mySQl", "3306"), @XmlEnumValue("PostgreSQL")
		PostgreSQL(JdbcLink.POSTGRESQL_DRIVER_CLASS_NAME, "PostgreSQL", "5432"), @XmlEnumValue("H2")
		H2(JdbcLink.H2_DRIVER_CLASS_NAME, "H2", StringUtils.EMPTY);

		public final String driverName;
		public final String dbType;
		public final String defaultPort;

		DBType(String driverName, String dbType, String defaultPort) {
			this.driverName = driverName;
			this.dbType = dbType;
			this.defaultPort = defaultPort;
		}

		public static Optional<DBType> valueOfIgnoreCase(String value) {
			for (DBType dbType : values()) {
				if (dbType.dbType.equalsIgnoreCase(value)) {
					return Optional.of(dbType);
				}
			}
			return Optional.empty();
		}

		public static Optional<DBType> valueOfDriver(String driver) {
			// compatibility for old mysql driver string
			if (driver.equals("com.mysql.jdbc.Driver")) {
				return Optional.of(MySQL);
			}
			for (DBType dbType : values()) {
				if (dbType.driverName.equals(driver)) {
					return Optional.of(dbType);
				}
			}
			return Optional.empty();
		}
	}

	public DBType rdbmsType;
	@XmlAttribute
	public String hostName;
	@XmlAttribute
	public String port;
	@XmlAttribute
	public String databaseName;
	@XmlAttribute
	public String connectionString;
	@XmlAttribute
	public String username;
	@XmlAttribute
	public String password;
	@XmlAttribute
	public String text;

	public DBConnection(DBType rdbmsType, String jdbcString, String username, char[] password) {
		this.rdbmsType = rdbmsType;
		this.connectionString = jdbcString;
		this.username = username;
		this.password = new String(password);
	}

	public DBConnection() {
	}

	/**
	 * are all required values for the DBConnection set?
	 *
	 * @return
	 */
	public boolean allValuesSet() {
		boolean result = true;
		if (rdbmsType == null) {
			result = false;
		}
		if (!DBType.H2.equals(rdbmsType) && StringUtils.isBlank(hostName)) {
			result = false;
		}
		if (StringUtils.isBlank(databaseName)) {
			result = false;
		}
		if (StringUtils.isBlank(username)) {
			result = false;
		}
		return result;
	}

	/**
	 * Marshall this object into a storable xml
	 *
	 * @param os
	 * @throws JAXBException
	 */
	public void marshall(OutputStream os) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(DBConnection.class);
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(this, os);
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

	public static Optional<String> getHostName(String url) {
		if (url == null || url.startsWith("jdbc:h2:")) {
			return Optional.empty();
		}
		if (url.startsWith("jdbc:")) {
			url = url.substring(5);
		}
		URI uri = URI.create(url);
		String host = uri.getHost();
		if (!StringUtils.isEmpty(host)) {
			return Optional.of(host);
		}
		return Optional.empty();
	}

	public static Optional<String> getDatabaseName(String url) {
		if (url.startsWith("jdbc:h2:")) {
			url = url.substring("jdbc:h2:".length());
			if (url.indexOf(';') > -1) {
				url = url.substring(0, url.indexOf(';'));
				return Optional.of(url);
			}
		}
		if (url.startsWith("jdbc:")) {
			url = url.substring(5);
		}
		URI uri = URI.create(url);
		String path = uri.getPath();
		if (!StringUtils.isBlank(path)) {
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			if (path.contains("/")) {
				path = path.substring(0, path.indexOf("/"));
			}
			return Optional.of(path);
		}
		return Optional.empty();
	}

}
