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
package ch.elexis.core.ui.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.commands.messages"; //$NON-NLS-1$
	public static String FallPlaneRechnung_PlanBillingAfterDays;
	public static String FallPlaneRechnung_PlanBillingHeading;
	public static String FallPlaneRechnung_PlanBillingPleaseEnterPositiveInteger;
	public static String MahnlaufCommand_Mahngebuehr1;
	public static String MahnlaufCommand_Mahngebuehr3;
	public static String MahnlaufCommand_Mahngebuehr2;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
