/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.eigenleistung;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.views.artikel.messages";//$NON-NLS-1$
	public static String EigenleistungContextMenu_create;
	public static String EigenleistungContextMenu_deleteAction;
	public static String EigenleistungContextMenu_deleteActionConfirmCaption;
	public static String EigenleistungContextMenu_deleteActionToolTip;
	public static String EigenleistungContextMenu_deleteConfirmBody;
	public static String EigenleistungContextMenu_newAction;
	public static String EigenleistungContextMenu_newActionTooltip;
	public static String EigenleistungContextMenu_pleaseEnterNameForArticle;
	public static String EigenleistungContextMenu_propertiesAction;
	public static String EigenleistungContextMenu_propertiesTooltip;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}