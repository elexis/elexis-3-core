/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.selectors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.selectors.messages";//$NON-NLS-1$
	
	public static String SelectorPanel_activateAutomaticSearch;
	public static String SelectorPanel_automaticSearch;
	public static String SelectorPanel_clearFields;
	public static String SelectorPanel_performSearch;
	public static String SelectorPanel_performSearchTooltip;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}