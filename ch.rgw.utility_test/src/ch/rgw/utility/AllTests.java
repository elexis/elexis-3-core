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

package ch.rgw.utility;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ch.rgw.tools.Test_JdbcLink.class, ch.rgw.tools.Test_Money.class
})
public class AllTests {
	public static Test suite() throws ClassNotFoundException{
		TestSuite suite = new TestSuite("ch.rgw.utility tests");
		return suite;
	}
}
