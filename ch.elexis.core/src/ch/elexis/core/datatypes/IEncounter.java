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
 * An IEncounter is a simple encounter between provider and client
 * 
 * @author gerry
 * 
 */
public interface IEncounter extends IPersistentObject {
	/** The date this encounter happened */
	public static final String FLD_DATE = "encounter_date";
	
	/** The ICustomerRelation this encounter belongs to */
	public static final String FLD_CUSTOMER_RELATION = "encounter_customer_relation";
	
	/** The entry describing this encounter */
	public static final String FLD_ENTRY = "encounter_entry";
	
	/** The IPartner responsible for this encounter */
	public static final String FLD_RESPONSIBLE = "encounter_responsible";
	
	/** The IPartner creating this encounter */
	public static final String FLD_EXECUTING = "encounter_executing";
	
	/** The bill that belongs to this encounter (if any) */
	public static final String FLD_BILL = "encounter_bill";
	
	/** The Reason for this encounter */
	public static final String FLD_RFE = "encounter_Reason";
}
