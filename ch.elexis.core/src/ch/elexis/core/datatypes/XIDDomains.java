/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    M. Descher - Initial implementation; Extraction of Xid.java
 * 
 *******************************************************************************/
package ch.elexis.core.datatypes;

/**
 * CENTRAL XID Domain Register
 * 
 * This class contains constants for the domains defined within Elexis, and a quality denominator
 * for the area they are valid in. If a domain is defined herein, one has to set the according
 * quality!
 * 
 * This is an extraction of Xid.java. The respective parts will be unhinged in the near future.
 * Please feel free to define additional XIDs in here!
 * 
 * @author Marco Descher <descher@medevit.at>
 * 
 */
public class XIDDomains {
	
	/**
	 * Quality value for an ID that is valid only in the context of the issuing program
	 */
	public static final int ASSIGNMENT_LOCAL = 1;
	/**
	 * Quality value for an ID that is valid within a geographic or politic context (e.g. a
	 * nationally assigned ID)
	 */
	public static final int ASSIGNMENT_REGIONAL = 2;
	/**
	 * Quality value for an ID that can be used as global identifier
	 */
	public static final int ASSIGNMENT_GLOBAL = 3;
	
	/**
	 * Marker that the ID is a GUID (that is, guaranteed to exist only once through time and space)
	 */
	public static final int QUALITY_GUID = 4;
	
	/**
	 * ELEXIS INTERNAL domain
	 */
	public static final String ELEXIS = "www.elexis.ch/xid";
	public static final int ELEXIS_QUALITY = ASSIGNMENT_LOCAL | QUALITY_GUID;
	
	/**
	 * GLOBAL
	 */
	public static final String EAN = "www.xid.ch/id/ean"; // European Article Number
	public static final int EAN_QUALITY = ASSIGNMENT_REGIONAL;
	public static final String OID = "www.xid.ch/id/oid"; // Object Identifier
	public static final int OID_QUALITY = ASSIGNMENT_GLOBAL | QUALITY_GUID;
	
	/**
	 * SWITZERLAND
	 */
	public static final String CH_PASSPORT = "www.xid.ch/id/passport/ch";
	public static final int CH_PASSPORT_QUALITY = ASSIGNMENT_GLOBAL;
	public static final String CH_AHV = "www.ahv.ch/xid"; // Alters- und Hinterbliebenenversicherung
	public static final int CH_AHV_QUALITY = ASSIGNMENT_REGIONAL;
	
	/**
	 * AUSTRIA
	 */
	public static final String AT_PASSPORT = "www.xid.ch/id/passport/at";
	public static final int AT_PASSPORT_QUALITY = ASSIGNMENT_GLOBAL;
	public static final String AT_SVNR = "www.sozialversicherung.at/svnr"; // Sozialversicherung
	public static final int AT_SVNR_QUALITY = ASSIGNMENT_REGIONAL;
	
	/**
	 * GERMANY
	 */
	public static final String DE_PASSPORT = "www.xid.ch/id/passport/de";
	public static final int DE_PASSPORT_QUALITY = ASSIGNMENT_GLOBAL;
	
}
