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
package ch.elexis.core.ui.text;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.text.messages"; //$NON-NLS-1$
	public static String EnhancedTextField_5;
	public static String EnhancedTextField_asMacro;
	public static String EnhancedTextField_copyAction;
	public static String EnhancedTextField_cutAction;
	public static String EnhancedTextField_enterNameforMacro;
	public static String EnhancedTextField_newMacro;
	public static String EnhancedTextField_pasteAction;
	public static String EnhancedTextField_RemoveXref;
	public static String EnhancedTextField_ThisChargeIsInvalid;
	public static String ExternalLink_CouldNotStartFile;
	public static String TextContainer_All;
	public static String TextContainer_BadFieldDefinition;
	public static String TextContainer_BadVariableFormat;
	public static String TextContainer_DontAskForAddressee;
	public static String TextContainer_EmptyDocument;
	public static String TextContainer_ErroneousLetter;
	public static String TextContainer_FieldTypeForContactsOnly;
	public static String TextContainer_FieldTypeForPersonsOnly;
	public static String TextContainer_Letter;
	public static String TextContainer_Mandator;
	public static String TextContainer_NoPlugin1;
	public static String TextContainer_NoPlugin2;
	public static String TextContainer_NoPlugin4;
	public static String TextContainer_NoPluginCaption;
	public static String TextContainer_NoPLugin5;
	public static String TextContainer_Noplugin3;
	public static String TextContainer_NullOpen;
	public static String TextContainer_NullSaveBody;
	public static String TextContainer_NullSaveHeader;
	public static String TextContainer_SaveDocumentBody;
	public static String TextContainer_SaveDocumentHeader;
	public static String TextContainer_SaveTemplateBody;
	public static String TextContainer_SaveTemplateHeader;
	public static String TextContainer_SelectAdresseeBody;
	public static String TextContainer_SelectAdresseeHeader;
	public static String TextContainer_SelectDestinationBody;
	public static String TextContainer_SelectDestinationHeader;
	public static String TextContainer_SystemTemplate;
	public static String TextContainer_Template;
	public static String TextContainer_TemplateExistsBody;
	public static String TextContainer_TemplateExistsCaption;
	public static String TextContainer_TemplateName;
	public static String TextContainer_TemplateNotFoundBody;
	public static String TextContainer_TemplateNotFoundHeader;
	public static String TextContainer_UnrecognizedFieldType;
	public static String TextContainer_TemplateTitleEmptyCaption;
	public static String TextContainer_TemplateTitleEmptyBody;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
