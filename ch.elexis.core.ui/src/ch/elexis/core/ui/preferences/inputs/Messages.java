/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.preferences.inputs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.preferences.inputs.messages";//$NON-NLS-1$
	
	public static String DecoratedStringChooser_howToChange;
	public static String KontaktFieldEditor_PleaseSelect;
	public static String KontaktFieldEditor_PleaseSelectContact;
	public static String KontaktFieldEditor_SelectContact;
	public static String MultiplikatorEditor_5;
	public static String MultiplikatorEditor_BegiNDate;
	public static String MultiplikatorEditor_NewMultipilcator;
	public static String MultiplikatorEditor_PleaseEnterBeginDate;
	public static String MultiplikatorEditor_add;
	public static String MultiplikatorEditor_from;
	public static String PrefAccessDenied_PageLocked;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}