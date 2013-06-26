/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.exchange;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.exchange.messages"; //$NON-NLS-1$
	public static String BlockContainer_Blockbeschreibung;
	public static String BlockContainer_xchangefiles;
	public static String KontaktMatcher_noauto1;
	public static String KontaktMatcher_noauto2;
	public static String KontaktMatcher_noauto3;
	public static String KontaktMatcher_noauto4;
	public static String KontaktMatcher_noauto5;
	public static String KontaktMatcher_OrganizationNotFound;
	public static String KontaktMatcher_OrganizationNotUnique;
	public static String KontaktMatcher_PersonNotFound;
	public static String KontaktMatcher_PersonNotUnique;
	public static String XChangeContainer_kg;
	public static String XChangeContainer_kontakte;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
