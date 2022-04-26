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

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.types.Gender;
import ch.elexis.data.Labor;
import ch.rgw.tools.TimeTool;
import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ Test_HL7_parser.class, MultiFileParserTests.class, HL7InitLabItemTest.class,
		TestPathologicDescription.class, Test_HL7Import_MPFRule.class, Test_Import_LabItemInconclusiveRefValue.class })
public class AllTests {

	public static Labor testLab;

	@BeforeClass
	public static void beforeClass() {
		testLab = new Labor("HL7_Test", "HL7_Test");

		TimeTool timeTool = new TimeTool();
		IPerson _mandator = new IContactBuilder.PersonBuilder(CoreModelServiceHolder.get(),
				"mandator1 " + timeTool.toString(), "Anton" + timeTool.toString(), timeTool.toLocalDate(), Gender.MALE)
						.mandator().buildAndSave();
		IMandator mandator = CoreModelServiceHolder.get().load(_mandator.getId(), IMandator.class).get();
		ContextServiceHolder.get().setActiveMandator(mandator);

	}

	public static Test suite() throws ClassNotFoundException {
		return new TestSuite("Importer Tests");
	}
}
