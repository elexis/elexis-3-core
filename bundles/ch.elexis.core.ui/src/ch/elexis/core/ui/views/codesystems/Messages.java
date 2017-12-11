/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.views.codesystems;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.views.codesystems.messages";//$NON-NLS-1$
	public static String BlockDetailDisplay_addPredefinedServices;
	public static String BlockDetailDisplay_addSelfDefinedServices;
	public static String BlockDetailDisplay_all;
	public static String BlockDetailDisplay_blocks;
	public static String BlockDetailDisplay_changeAction;
	public static String BlockDetailDisplay_changeActionTooltip;
	public static String BlockDetailDisplay_code;
	public static String BlockDetailDisplay_costInCents;
	public static String BlockDetailDisplay_defineServiceBody;
	public static String BlockDetailDisplay_defineServiceCaption;
	public static String BlockDetailDisplay_editServiceBody;
	public static String BlockDetailDisplay_editServiceCaption;
	public static String BlockDetailDisplay_moveDown;
	public static String BlockDetailDisplay_moveUp;
	public static String BlockDetailDisplay_name;
	public static String BlockDetailDisplay_macro;
	public static String BlockDetailDisplay_priceInCents;
	public static String BlockDetailDisplay_remove;
	public static String BlockDetailDisplay_SerlfDefinedService;
	public static String BlockDetailDisplay_services;
	public static String BlockDetailDisplay_shortname;
	public static String BlockDetailDisplay_timeInMinutes;
	public static String BlockDetailDisplay_title;
	public static String BlockImporter_Blocks;
	public static String BlockImporter_importBlocks;
	public static String CodeDetailView_errorBody;
	public static String CodeDetailView_errorCaption;
	public static String CodeDetailView_importActionTitle;
	public static String CodeDetailView_importerCaption;
	public static String CodeSelectorFactory_16;
	public static String CodeSelectorFactory_all;
	public static String CodeSelectorFactory_error;
	public static String CodeSelectorFactory_patientsMostFrequent;
	public static String CodeSelectorFactory_yourMostFrequent;
	public static String CodeSelectorFactory_resetStatistic;
	public static String LeistungenView_error;
	public static String LeistungenView_defineColor;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}