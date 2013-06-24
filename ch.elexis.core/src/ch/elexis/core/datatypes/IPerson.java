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
 * An IPerson is an IPartner that is a human.
 * 
 * @author gerry
 * 
 */
public interface IPerson extends IPartner {
	public static final String MALE = "m";
	public static final String FEMALE = "f";
	public static final String FLD_FIRSTNAME = "person_firstname";
	public static final String FLD_LASTNAME = "person_lastname";
	public static final String FLD_SEX = "person_sex";
	public static final String FLD_BIRTHDATE = "person_dob";
	public static final String FLD_TITLE = "person_title";
	public static final String FLD_MOBILE = "person_mobilephone";
}
