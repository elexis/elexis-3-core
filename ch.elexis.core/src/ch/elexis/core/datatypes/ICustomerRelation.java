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
 * An ICustomerRelation is a description of a client relationship. Who should receive the bills, who
 * will pay, what is the reason for the relationship and so on. This replaces the "Fall" in Elexis
 * 2.x
 * 
 * @author gerry
 * 
 */
public interface ICustomerRelation extends IPersistentObject {
	/**
	 * the debtor is the IPartner that receives the bills and is directly responsible for payment
	 */
	public static final String FLD_DEBTOR_ID = "crl_debtor_id";
	/**
	 * The guarantor is the IPartner that will eventually refund the payment to the debtor. debtor
	 * and guarantor can be the same.
	 */
	public static final String FLD_GUARANTOR_ID = "clr_guarantor_id";
	
	/**
	 * description of the applicable law for this relationship
	 */
	public static final String FLD_LAW = "clr_law";
	
	/**
	 * Description of the reason for this relationship
	 */
	public static final String FLD_REASON = "clr_reason";
	
}
