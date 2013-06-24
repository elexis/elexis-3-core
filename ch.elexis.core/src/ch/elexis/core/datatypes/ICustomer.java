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
 * An IPartner with a customer/patient/client relationship to us. Note that an ICustomer is not
 * necessarily an IDebtor since sometimes somebody else has to pay (e.g. parents for their children
 * or insurances for their insurees)
 * 
 * @author gerry
 * 
 */
public interface ICustomer extends IPartner {
	public static final String TYPID = ICustomer.class.getName();
	
	public ICustomerRelation[] getCustomerRelations();
	
	public void addCustomerRelation(ICustomerRelation cr);
}
