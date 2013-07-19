/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.matchers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.matchers.messages"; //$NON-NLS-1$
	public static String ConflictResolveDialog_CannotDecideAutomatically;
	public static String ConflictResolveDialog_ImportCaption;
	public static String ConflictResolveDialog_OrCreateNew;
	public static String ConflictResolveDialog_Pleaseselectbelow;
	public static String ConflictResolveDialog_whethercontainedinDatavase;
	public static String Verifier_AddressHeading;
	public static String Verifier_NameHeading;
	public static String Verifier_PleaseSelectCorrectContact;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
