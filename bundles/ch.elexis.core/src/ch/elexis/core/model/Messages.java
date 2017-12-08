/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ch.elexis.core.model;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "ch.elexis.core.model.messages";//$NON-NLS-1$
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private Messages(){
		// Do not instantiate
	}

	public static String Fall_Accident;
	public static String Fall_Birthdefect;
	public static String Fall_Disease;
	public static String Fall_Maternity;
	public static String Fall_Other;
	public static String Fall_Prevention;

	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	public static String getString(String key){
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException missingResourceException) {
			return '!' + key + '!';
		}
	}
}