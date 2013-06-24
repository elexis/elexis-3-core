/*******************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.datatypes;

/**
 * An IPartner is just anybody or anything that contacts us
 * 
 * @author gerry
 * 
 */
public interface IPartner extends IPersistentObject {
	public String TYPID = IPartner.class.getName();
	public String FLD_NAME = "partner_name";
	public String FLD_FIRSTNAME = "partner_firstname";
	
	public String getContactSalutation();
	
	public ContactInfo[] getContactInfos();
	
	public ContactInfo getContactInfo(ContactInfo.CTYPE type);
}
