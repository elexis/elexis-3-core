/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.commands.messages";//$NON-NLS-1$
	
	public static String AbortLocalDocumentHandler_infomessage;

	public static String AbortLocalDocumentHandler_infotitle;

	public static String EndLocalDocumentHandler_conflictmessage;

	public static String EndLocalDocumentHandler_conflicttitle;

	public static String EndLocalDocumentHandler_errormessage;

	public static String EndLocalDocumentHandler_errorttitle;

	public static String EndLocalDocumentHandler_infomessage;

	public static String EndLocalDocumentHandler_infotitle;

	public static String FallPlaneRechnung_PlanBillingAfterDays;
	public static String FallPlaneRechnung_PlanBillingHeading;
	public static String FallPlaneRechnung_PlanBillingPleaseEnterPositiveInteger;
	
	public static String FallCopyCommand_RelatedConsultations;
	public static String FallCopyCommand_TransferConsultations;
	public static String FallCopyCommand_AttentionTransferConsultations;
	public static String LoadTemplateCommand_Error;
	public static String LoadTemplateCommand_NoTextTemplate;
	
	public static String StartEditLocalDocumentHandler_alreadyOpenEnd;

	public static String StartEditLocalDocumentHandler_alreadyOpenStart;

	public static String StartEditLocalDocumentHandler_conflictmessage;

	public static String StartEditLocalDocumentHandler_conflicttitle;

	public static String StartEditLocalDocumentHandler_errormessage;

	public static String StartEditLocalDocumentHandler_errortitle;

	public static String StartEditLocalDocumentHandler_warning;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}