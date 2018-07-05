/*******************************************************************************
 * Copyright (c) 2010, Elexis und Niklaus Giger <niklaus.giger@member.fsf.org
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    N. Giger - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.importer.div;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.elexis.data.Labor;
import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	Test_HL7_parser.class, MultiFileParserTests.class, HL7InitLabItemTest.class,
	TestPathologicDescription.class, Test_HL7Import_MPFRule.class, Test_Import_LabItemInconclusiveRefValue.class
})
public class AllTests {
	
	public static Labor testLab;
	
	@BeforeClass
	public static void beforeClass(){
		testLab = new Labor("HL7_Test", "HL7_Test");
	}
	
	public static Test suite() throws ClassNotFoundException{
		return new TestSuite("Importer Tests");
	}
}
