/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.rgw.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Test;

import ch.rgw.tools.JdbcLink.Stm;

public class Test_JdbcLink {
	
	@Test
	public void testConnect(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem ", "");
		link.connect("", "");
		link.disconnect();
	}
	
	@Test
	public void testConnectDriverFail(){
		JdbcLink link = new JdbcLink("", "", "");
		try {
			link.connect("", "");
			fail("Expected Exception not thrown!");
		} catch (JdbcLinkException pe) {
			
		}
	}
	
	@Test
	public void testConnectDatabaseConnectFail(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "", "");
		try {
			link.connect("", "");
			fail("Expected Exception not thrown!");
		} catch (JdbcLinkException je) {
			
		}
	}
	
	@Test
	public void testGetStatement(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		link.connect("", "");
		Stm stm = link.getStatement();
		assertNotNull(stm);
		link.disconnect();
	}
	
	@Test
	public void testGetStatementConnectFail(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "", "");
		try {
			link.getStatement();
			fail("Expected Exception not thrown!");
		} catch (JdbcLinkException je) {
			
		}
	}
	
	@Test
	public void testPrepareStatement(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		link.connect("", "");
		PreparedStatement stm = link.prepareStatement("");
		assertNotNull(stm);
		link.disconnect();
	}
	
	@Test
	public void testPrepareStatementConnectFail(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "", "");
		try {
			link.prepareStatement("");
			fail("Expected Exception not thrown!");
		} catch (JdbcLinkException je) {
			
		}
	}
	
	@Test
	public void testExec(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		link.connect("", "");
		int rows = link.exec("");
		assertEquals(0, rows);
		link.disconnect();
	}
	
	@Test
	public void testExecConnectFail(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		link.connect("", "");
		link.disconnect();
		try {
			link.exec("");
			fail("Expected Exception not thrown!");
		} catch (JdbcLinkException je) {
			
		}
	}
	
	@Test
	public void testStmExecConnectFail(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		link.connect("", "");
		Stm stm = link.getStatement();
		assertNotNull(stm);
		link.disconnect();
		stm.delete();
		try {
			stm.exec("");
			fail("Expected Exception not thrown!");
		} catch (JdbcLinkException je) {
			
		}
	}
	
	@Test
	public void testStmQuery(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		link.connect("", "");
		Stm stm = link.getStatement();
		assertNotNull(stm);
		stm.exec("CREATE TABLE ABC (ID INTEGER)");
		ResultSet set = stm.query("SELECT * FROM ABC");
		assertNotNull(set);
		stm.exec("DROP TABLE ABC");
		link.disconnect();
	}
	
	@Test
	public void testStmQueryConnectFail(){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "");
		link.connect("", "");
		Stm stm = link.getStatement();
		assertNotNull(stm);
		stm.exec("CREATE TABLE ABC (ID INTEGER)");
		link.disconnect();
		stm.delete();
		try {
			stm.query("SELECT * FROM ABC");
			fail("Expected Exception not thrown!");
		} catch (JdbcLinkException je) {
			
		}
	}
}
