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

package ch.elexis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.IXid;
import ch.rgw.tools.Log;
import ch.rgw.tools.VersionInfo;

/**
 * A XID is an external identifier, that is an ID from en external identifyer system. Examples are
 * entities such as Social Security Number, Passport Number, OID, and others. XID's are not limites
 * to Persons but can be attributed to all kinds of entities. They are usable like OID's but are
 * more general and can integrate other systems. A XID consists of a domain that denotes the
 * identifying system and an ID within this domain. The Domain name is globally unique, while the ID
 * can be globally unique, but might also be unique within its domain only. To differentiate between
 * such "qualities" there is a flag that indicates, that a XID is in fact a GUID. There is also a
 * flag to indicate the range within a xid is valid (e.g. a social security number ist only an
 * identifier within the country where it was created) The Flag ASSIGNMENT_LOCAL means that this XID
 * is used only between different instances of this program, ASSIGNMENT_REGIONAL means a XID used
 * within a country while ASSIGNMENT_GLOBAL a globally used identifier is (e.g. an EAN or an OID)
 * 
 * To simplify working with XIDs for the user, a XID Domain can also have a short "nickname" that is
 * valid and unique within the running instance of the program only and that is mapped to a full
 * xid-domain. The Domain "www.xid.ch/id/ean" can be called "EAN". The two namings ar exchangeable
 * within the defining instance, but a XID that leaves this instance of the program must always be
 * named with its full name.
 * 
 * @author Gerry
 * 
 */
public class Xid extends PersistentObject implements IXid {
	
	public static final String FLD_OBJECT = "object";
	public static final String FLD_ID_IN_DOMAIN = "domain_id";
	public static final String FLD_DOMAIN = "domain";
	public static final String FLD_QUALITY = "quality";
	public static final String FLD_TYPE = "type";
	
	private static final String VERSION = "1.0.0";
	private static final String TABLENAME = "XID";
	private static Log log = Log.get("XID");
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
	
	private static HashMap<String, XIDDomain> domains;
	private static HashMap<String, String> domainMap;
	
	public static final String DOMAIN_ELEXIS = "www.elexis.ch/xid";
	public static final String DOMAIN_AHV = "www.ahv.ch/xid";
	public static final String DOMAIN_SWISS_PASSPORT = "www.xid.ch/id/passport/ch";
	public static final String DOMAIN_AUSTRIAN_PASSPORT = "www.xid.ch/id/passport/at";
	public static final String DOMAIN_GERMAN_PASSPORT = "www.xid.ch/id/passport/de";
	public static final String DOMAIN_EAN = "www.xid.ch/id/ean";
	public static final String DOMAIN_RECIPIENT_EAN = "www.xid.ch/id/recipient_ean";
	public static final String DOMAIN_OID = "www.xid.ch/id/oid";
	
	static {
		addMapping(TABLENAME, FLD_TYPE, FLD_OBJECT, FLD_DOMAIN, FLD_ID_IN_DOMAIN, FLD_QUALITY);
		domains = new HashMap<String, XIDDomain>();
		domainMap = new HashMap<String, String>();
		String storedDomains = CoreHub.globalCfg.get("LocalXIDDomains", null);
		if (storedDomains == null) {
			domains.put(XidConstants.ELEXIS,
				new XIDDomain(XidConstants.ELEXIS, "UUID", XidConstants.ELEXIS_QUALITY
					| QUALITY_GUID, PersistentObject.class.getCanonicalName()));
			domains.put(XidConstants.CH_AHV, new XIDDomain(XidConstants.CH_AHV, "AHV",
				XidConstants.CH_AHV_QUALITY, Person.class.getCanonicalName()));
			domains.put(DOMAIN_OID, new XIDDomain(DOMAIN_OID, "OID", ASSIGNMENT_GLOBAL
				| QUALITY_GUID, PersistentObject.class.getCanonicalName()));
			domains.put(DOMAIN_EAN, new XIDDomain(DOMAIN_EAN, "EAN", ASSIGNMENT_REGIONAL,
				Kontakt.class.getCanonicalName()));
			storeDomains();
		} else {
			for (String dom : storedDomains.split(";")) {
				String[] spl = dom.split("#");
				if (spl.length < 2) {
					log.log("Fehler in XID-Domain " + dom, Log.ERRORS);
				}
				String simpleName = "";
				if (spl.length >= 3) {
					simpleName = spl[2];
				}
				String displayOptions = "Kontakt";
				if (spl.length >= 4) {
					displayOptions = spl[3];
				}
				domains.put(spl[0], new XIDDomain(spl[0], simpleName, Integer.parseInt(spl[1]),
					displayOptions));
				domainMap.put(simpleName, spl[0]);
			}
		}
		VersionInfo vv = new ch.rgw.tools.VersionInfo(CoreHub.Version);
		if (vv.isOlder("1.3.2")) {
			XIDDomain xd = domains.get(DOMAIN_EAN);
			xd.addDisplayOption(Person.class);
			xd.addDisplayOption(Organisation.class);
			xd = domains.get(DOMAIN_AHV);
			xd.addDisplayOption(Person.class);
		}
	}
	
	/**
	 * create a new XID. Does nothing if identical XID already exists.
	 * 
	 * @param o
	 *            the object to identify with the new XID
	 * @param domain
	 *            the domain from wich the identifier is (e.g. DOMAIN_COVERCARD). Must be a
	 *            registered domain
	 * @param domain_id
	 *            the id from that domain that identifies the object
	 * @param quality
	 *            the quality of this identifier
	 * @throws XIDException
	 *             if a XID with same domain and domain_id but different object or quality already
	 *             exists. if the domain was not rgeistered
	 */
	public Xid(final PersistentObject o, final String domain, final String domain_id)
		throws XIDException{
		XIDDomain dom = domains.get(domain);
		if (dom == null) {
			throw new XIDException("Domain not registered: " + domain);
		}
		Integer val = dom.quality;
		if (val == null) {
			throw new XIDException("XID Domain " + domain + " is not registered");
		}
		if (val > 9) {
			val = (val & 7) + 4;
		}
		Xid xid = findXID(domain, domain_id);
		if (xid != null) {
			if (xid.get(FLD_OBJECT).equals(o.getId())) {
				return;
			}
			throw new XIDException("XID " + domain + ":" + domain_id + " is not unique");
		}
		xid = findXID(o, domain);
		if (xid != null) {
			throw new XIDException("XID " + domain + ": " + domain_id + " was already assigned");
		}
		create(null);
		set(new String[] {
			FLD_TYPE, FLD_OBJECT, FLD_DOMAIN, FLD_ID_IN_DOMAIN, FLD_QUALITY
		}, new String[] {
			o.getClass().getName(), o.getId(), domain, domain_id, Integer.toString(val)
		});
	}
	
	/**
	 * Get the quality of this xid
	 * 
	 * @return the quality
	 */
	public int getQuality(){
		return checkZero(get(FLD_QUALITY));
	}
	
	/**
	 * Tell whether this XID is a GUID
	 * 
	 * @return true if so.
	 */
	public boolean isGUID(){
		return (getQuality() & QUALITY_GUID) != 0;
	}
	
	/**
	 * get the Domain this Xid is from
	 * 
	 * @return
	 */
	public String getDomain(){
		return get(FLD_DOMAIN);
	}
	
	/**
	 * get the id of this Xid in its domain
	 * 
	 * @return
	 */
	public String getDomainId(){
		return get(FLD_ID_IN_DOMAIN);
	}
	
	/**
	 * Get the object that is identified with this XID
	 * 
	 * @return the object or null if it could not be restored.
	 */
	public IPersistentObject getObject(){
		PersistentObject po =
			CoreHub.poFactory.createFromString(get(FLD_TYPE) + "::" + get(FLD_OBJECT));
		return po;
	}
	
	@Override
	public String getLabel(){
		IPersistentObject po = getObject();
		String text = "unknown object";
		if (po != null) {
			text = po.getLabel();
		}
		StringBuilder ret = new StringBuilder();
		ret.append(text).append(": ").append(get(FLD_DOMAIN)).append("->")
			.append(get(FLD_ID_IN_DOMAIN));
		return ret.toString();
	}
	
	public static Xid load(final String id){
		return new Xid(id);
	}
	
	/**
	 * Find a XID from a domain and a domain_id
	 * 
	 * @param domain
	 *            the domain to search an id from, Can be full name or local short name of the
	 *            domain
	 * @param id
	 *            the id out of domain to retrieve
	 * @return the xid holding that id from that domain or null if no such xid was found
	 */
	public static Xid findXID(String domain, final String id){
		String dom = domainMap.get(domain);
		if (dom != null) {
			domain = dom;
		}
		Query<Xid> qbe = new Query<Xid>(Xid.class);
		qbe.add(FLD_DOMAIN, Query.EQUALS, domain);
		qbe.add(FLD_ID_IN_DOMAIN, Query.EQUALS, id);
		List<Xid> ret = qbe.execute();
		if (ret.size() == 1) {
			Xid result = ret.get(0);
			IPersistentObject po = result.getObject();
			if (po != null && po.exists()) {
				return result;
			} else {
				result.delete();
			}
		}
		return null;
	}
	
	/**
	 * Find a PersistentObject from a domain and a domain_id
	 * 
	 * @param domain
	 *            the domain to search an id from (e.g. www.ahv.ch)
	 * @param id
	 *            the id out of domain to retrieve
	 * @return the PersistentObject identified by that id from that domain or null if no such Object
	 *         was found
	 */
	public static IPersistentObject findObject(final String domain, final String id){
		Xid xid = findXID(domain, id);
		if (xid != null) {
			return xid.getObject();
		}
		return null;
	}
	
	/**
	 * Find the Xid of a given domain for the given Object
	 * 
	 * @param o
	 *            the object whose Xid should be find
	 * @param domain
	 *            the domain the Xid should be from
	 * @return the Xid or null if no xid for the given domain was found on the given object
	 */
	public static Xid findXID(final PersistentObject o, String domain){
		String dom = domainMap.get(domain);
		if (dom != null) {
			domain = dom;
		}
		Query<Xid> qbe = new Query<Xid>(Xid.class);
		qbe.add(FLD_DOMAIN, Query.EQUALS, domain);
		qbe.add(FLD_OBJECT, Query.EQUALS, o.getId());
		List<Xid> ret = qbe.execute();
		if (ret.size() == 1) {
			return ret.get(0);
		}
		return null;
	}
	
	/**
	 * Register a new domain for use with our XID System locally (this will not affect the central
	 * XID registry at www.xid.ch)
	 * 
	 * @param domain
	 *            the domain to register
	 * @param quality
	 *            the quality an ID of that domain will have
	 * @return true on success, false if that domain could not be registered
	 */
	public static boolean localRegisterXIDDomain(final String domain, String simpleName,
		final int quality){
		if (domains.containsKey(domain)) {
			log.log("XID Domain " + domain + " bereits registriert", Log.ERRORS);
		} else {
			if (domain.matches(".*[;#].*")) {
				log.log("XID Domain " + domain + " ungültig", Log.ERRORS);
			} else {
				domains.put(domain, new XIDDomain(domain, simpleName == null ? "" : simpleName,
					quality, "Kontakt"));
				if (simpleName != null) {
					domainMap.put(simpleName, domain);
				}
				storeDomains();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Register a local xid domain if it does not exist. Does nothing if a domain with the given
	 * domain name exists already
	 * 
	 * @param domain
	 *            name of the domain
	 * @param simpleName
	 *            short name for the domain
	 * @param quality
	 *            the wuality of an ID of that domain will have
	 * @return true on success
	 */
	public static boolean localRegisterXIDDomainIfNotExists(final String domain, String simpleName,
		final int quality){
		if (domains.get(domain) != null) {
			return true;
		}
		return localRegisterXIDDomain(domain, simpleName, quality);
	}
	
	/**
	 * Get the ID quality of an Object of a given domain
	 * 
	 * @param xidDomain
	 *            the domain to query
	 * @return obne of the Quality-ID constants or null if no such domain ist registered
	 */
	public static Integer getXIDDomainQuality(final String xidDomain){
		XIDDomain xd = domains.get(xidDomain);
		if (xd == null) {
			return null;
		}
		return xd.getQuality();
	}
	
	public static String getSimpleNameForXIDDomain(final String domain){
		XIDDomain xd = domains.get(domain);
		if (xd == null) {
			return domain;
		}
		return xd.simple_name;
	}
	
	public static XIDDomain getDomain(String name){
		String dom = domainMap.get(name);
		if (dom != null) {
			name = dom;
		}
		return domains.get(name);
	}
	
	protected Xid(final String id){
		super(id);
	}
	
	protected Xid(){}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@SuppressWarnings("serial")
	public static class XIDException extends Exception {
		public XIDException(final String reason){
			super(reason);
		}
	}
	
	private static void storeDomains(){
		StringBuilder sb = new StringBuilder();
		for (String k : domains.keySet()) {
			XIDDomain xd = domains.get(k);
			sb.append(k).append("#").append(xd.getQuality()).append("#").append(xd.getSimpleName())
				.append("#").append(xd.getDisplayOptions()).append(";");
		}
		CoreHub.globalCfg.set("LocalXIDDomains", sb.toString());
	}
	
	/**
	 * return a list of all known domains
	 * 
	 * @return
	 */
	public static Set<String> getXIDDomains(){
		return domains.keySet();
	}
	
	@Override
	public boolean undelete(){
		new DBLog(this, DBLog.TYP.UNDELETE);
		return (set("deleted", "0"));
	};
	
	public static class XIDDomain {
		String domain_name;
		String simple_name;
		int quality;
		ArrayList<Class<? extends PersistentObject>> displayOptions =
			new ArrayList<Class<? extends PersistentObject>>();
		
		@SuppressWarnings("unchecked")
		public XIDDomain(String dname, String simplename, int quality, String options){
			domain_name = dname;
			simple_name = simplename;
			this.quality = quality;
			for (String op : options.split(",")) {
				try {
					Class clazz = Class.forName(op);
					displayOptions.add(clazz);
				} catch (Exception ex) {}
			}
		}
		
		public String getSimpleName(){
			return simple_name;
		}
		
		public void setSimpleName(String simple_name){
			this.simple_name = simple_name;
			storeDomains();
		}
		
		public String getDomainName(){
			return domain_name;
		}
		
		public int getQuality(){
			return quality;
		}
		
		public void addDisplayOption(Class<? extends PersistentObject> clazz){
			if (!displayOptions.contains(clazz)) {
				displayOptions.add(clazz);
				storeDomains();
			}
		}
		
		public void removeDisplayOption(Class<? extends PersistentObject> clazz){
			displayOptions.remove(clazz);
			storeDomains();
		}
		
		public boolean isDisplayedFor(Class<? extends PersistentObject> clazz){
			return displayOptions.contains(clazz);
		}
		
		String getDisplayOptions(){
			StringBuilder r = new StringBuilder();
			for (Class<? extends PersistentObject> clazz : displayOptions) {
				r.append(clazz.getName()).append(",");
			}
			return r.toString();
		}
	}
	
}
