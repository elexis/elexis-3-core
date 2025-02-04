package ch.elexis.core.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class JAXBTest {

	@Test
	public void jaxbFunctionality() throws JAXBException, IOException {
		DBConnection dbc = new DBConnection();
		dbc.databaseName = "testDatabase";
		dbc.hostName = "testHost";
		dbc.password = "testPassword";
		dbc.rdbmsType = DBType.H2;
		dbc.username = "testUsername";

		String marshallIntoString = dbc.marshallIntoString();
		assertNotNull(marshallIntoString);
		assertNotEquals(StringUtils.EMPTY, marshallIntoString);
		System.out.println("DBConnection: [" + marshallIntoString + "]");

		DBConnection _dbc = DBConnection.unmarshall(marshallIntoString);
		assertEquals(dbc.databaseName, _dbc.databaseName);
		assertEquals(dbc.hostName, _dbc.hostName);
		assertEquals(dbc.password, _dbc.password);
		assertEquals(dbc.rdbmsType, _dbc.rdbmsType);
	}

	@Test
	public void test() throws JAXBException, IOException {
		DBConnection dbc = new DBConnection();
		dbc.databaseName = "testDatabase";
		dbc.hostName = "testHost";
		dbc.password = "testPassword";
		dbc.rdbmsType = DBType.H2;
		dbc.username = "testUsername";

		try (StringWriter sw = new StringWriter()) {
			JAXBContext jaxbContext = JAXBContext.newInstance(DBConnection.class);
			Marshaller m = jaxbContext.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(dbc, sw);
			System.out.println(sw.toString());
		}
	}

}
