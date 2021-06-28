package ch.elexis.core.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;

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
		System.out.println(marshallIntoString);
		
		DBConnection _dbc = DBConnection.unmarshall(marshallIntoString);
		assertEquals(dbc.databaseName, _dbc.databaseName);
		assertEquals(dbc.hostName, _dbc.hostName);
		assertEquals(dbc.password, _dbc.password);
		assertEquals(dbc.rdbmsType, _dbc.rdbmsType);
	}

	
}
