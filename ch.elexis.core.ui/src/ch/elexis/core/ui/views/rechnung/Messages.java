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
package ch.elexis.core.ui.views.rechnung;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import ch.rgw.tools.ExHandler;

public class Messages {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.views.rechnung.messages"; //$NON-NLS-1$
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private Messages(){}
	
	public static String getString(String key){
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static String getString(String key, Object[] params){
		if (params == null) {
			return getString(key);
		}
		try {
			return java.text.MessageFormat.format(getString(key), params);
		} catch (Exception e) {
			ExHandler.handle(e);
			return "!" + key + "!";
		}
	}
}
