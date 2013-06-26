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
package ch.elexis.core.ui.views.textsystem;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.views.textsystem.messages"; //$NON-NLS-1$
	public static String AbstractProperties_message_FileNotFound;
	public static String PlatzhalterProperties_label_no_category;
	public static String PlatzhalterProperties_message_empty;
	public static String PlatzhalterProperties_tooltip_no_category;
	public static String PlatzhalterView_menu_copy;
	public static String PlatzhalterView_message_Info;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
