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
 * An IXid is an external identifier for an Object
 * 
 * @author gerry
 * 
 */
public interface IXid {
	
	public static final String QUALITY = "quality";
	public static final String ID_IN_DOMAIN = "domain_id";
	public static final String TYPE = "type";
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
	public static final String DOMAIN_ELEXIS = "www.elexis.ch/xid";
	public static final String DOMAIN_AHV = "www.ahv.ch/xid";
	public static final String DOMAIN_SWISS_PASSPORT = "www.xid.ch/id/passport/ch";
	public static final String DOMAIN_AUSTRIAN_PASSPORT = "www.xid.ch/id/passport/at";
	public static final String DOMAIN_GERMAN_PASSPORT = "www.xid.ch/id/passport/de";
	public final static String DOMAIN_EAN = "www.xid.ch/id/ean";
	public final static String DOMAIN_OID = "www.xid.ch/id/oid";
	public static final String FLD_OBJECT = "object";
	public static final String FLD_DOMAIN_ID = "domain_id";
	public static final String FLD_DOMAIN = "domain";
	
	/**
	 * Get the quality of this xid
	 * 
	 * @return the quality
	 */
	public abstract int getQuality();
	
	/**
	 * Tell whether this XID is a GUID
	 * 
	 * @return true if so.
	 */
	public abstract boolean isGUID();
	
	/**
	 * get the Domain this Xid is from
	 * 
	 * @return
	 */
	public abstract String getDomain();
	
	/**
	 * get the id of this Xid in its domain
	 * 
	 * @return
	 */
	public abstract String getDomainId();
	
	/**
	 * Get the object that is identified with this XID
	 * 
	 * @return the object or null if it could not be restored.
	 */
	public abstract IPersistentObject getObject();
	
	public abstract String getLabel();
	
	public String getId();
}