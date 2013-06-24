/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.data.admin;

import static ch.elexis.core.data.admin.AccessControlDefaults.*;

/**
 * Contribution of the basic system's ACLs
 * 
 * @author gerry
 * 
 */
public class ACLContributor implements IACLContributor {
	private final ACE[] acls = new ACE[] {
		ACCOUNTING_GLOBAL, ADMIN_ACE, ACCOUNTING_BILLCREATE, ACCOUNTING_BILLMODIFY,
		ACCOUNTING_READ, ACCOUNTING_STATS, ACL_USERS, DATA, KONTAKT, PATIENT, KONTAKT_DELETE,
		DELETE, DELETE_FORCED, KONTAKT_DISPLAY, KONTAKT_INSERT, KONTAKT_MODIFY, KONTAKT_EXPORT,
		KONTAKT_ETIKETTE, PATIENT_DISPLAY, PATIENT_INSERT, PATIENT_MODIFY, LAB_SEEN,
		LSTG_VERRECHNEN, LSTG_CHARGE_FOR_ALL, KONS_CREATE, KONS_DELETE, KONS_EDIT, KONS_REASSIGN,
		AC_ABOUT, AC_CHANGEMANDANT, AC_CONNECT, AC_EXIT, AC_HELP, AC_IMORT, AC_LOGIN, AC_PREFS,
		AC_PURGE, AC_SHOWPERSPECTIVE, AC_SHOWVIEW, DOCUMENT, DOCUMENT_CREATE, DOCUMENT_SYSTEMPLATE,
		DOCUMENT_TEMPLATE, ADMIN_CHANGE_BILLSTATUS_MANUALLY, ADMIN_KONS_EDIT_IF_BILLED,
		ADMIN_VIEW_ALL_REMINDERS, MEDICATION_MODIFY, DELETE_MEDICATION, DELETE_LABITEMS,
		CASE_MODIFY, CASE_MODIFY_SPECIALS, CASE_DEFINE_SPECIALS, SCRIPT_EXECUTE, SCRIPT_EDIT,
		ACE_DOCUMENTS, ACE_DOC_CREATE, ACE_DOC_DELETE, ACE_DOC_CATCREATE, ACE_DOC_CATDELETE
	
	};
	
	public ACE[] getACL(){
		return acls;
	}
	
	public ACE[] reject(final ACE[] acl){
		// TODO Management of collisions
		return null;
	}
	
}
