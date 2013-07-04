/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.application.advisors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.application.advisors.messages";//$NON-NLS-1$
	public static String ApplicationActionBarAdvisor_3;
	public static String ApplicationActionBarAdvisor_4;
	public static String ApplicationActionBarAdvisor_5;
	public static String ApplicationActionBarAdvisor_6;
	public static String ApplicationActionBarAdvisor_7;
	public static String ApplicationActionBarAdvisor_9;
	public static String ApplicationWorkbenchAdvisor_7;
	public static String ApplicationWorkbenchAdvisor_8;
	public static String ApplicationWorkbenchAdvisor_9;
	public static String ApplicationActionBarAdvisor_10;
	public static String ApplicationWorkbenchAdvisor_10;
	public static String Hub_message_birthday;
	public static String Hub_message_configuration;
	public static String Hub_message_reminders;
	public static String Hub_nomandantor;
	public static String Hub_nopatientselected;
	public static String Hub_nouserloggedin;
	public static String Hub_title_configuration;
	public static String LoginDialog_0;
	public static String LoginDialog_1;
	public static String LoginDialog_4;
	public static String LoginDialog_login;
	public static String LoginDialog_terminate;
	public static String ReminderView_importantRemindersOnLogin;
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}