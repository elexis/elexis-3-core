/*******************************************************************************
 * Copyright (c) 2009-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.exchange;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.core.data.Xid;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.IXid;
import ch.elexis.core.ui.exchange.elements.XidElement;
import ch.rgw.tools.TimeTool;

/**
 * A MetaXid is a format for Xid that allows comparing and merging of Xids from different sources
 * 
 * @author gerry
 * 
 */
public class MetaXid {
	String id;
	List<Identity> identities = new ArrayList<Identity>();
	
	/*
	 * Construct a MetaXid from an XML Element
	 */
	public MetaXid(Element xidElement){
		id = xidElement.getAttributeValue("id");
		List<Element> ids = xidElement.getChildren();
		for (Element identity : ids) {
			if (identity.getName().equalsIgnoreCase("identity")) {
				Identity i =
					new Identity(identity.getAttributeValue("domain"),
						identity.getAttributeValue("domainID"),
						mapQuality(identity.getAttributeValue("quality")),
						Boolean.parseBoolean(identity.getAttributeValue("isGUID")),
						identity.getAttributeValue("date"));
				identities.add(i);
			}
		}
	}
	
	/**
	 * Construct a MetaXid from a Elexis-Xid
	 * 
	 * @param xidObject
	 */
	public MetaXid(Xid xidObject){
		this(xidObject.getObject());
	}
	
	/**
	 * Construct a MetaXid from an Elexis-PersistentObject
	 * 
	 * @param obj
	 */
	public MetaXid(IPersistentObject obj){
		List<IXid> xids = obj.getXids();
		String bestID = obj.getId();
		int bestQuality = Xid.ASSIGNMENT_LOCAL;
		boolean bestIsGuid = true;
		for (IXid xid : xids) {
			Identity i =
				new Identity(xid.getDomain(), xid.getDomainId(), xid.getQuality(), xid.isGUID(),
					new TimeTool(((Xid) xid).getLastUpdate()).toString(TimeTool.DATE_ISO));
			identities.add(i);
			if (i.quality > bestQuality) {
				if (i.isGUID) {
					bestID = xid.getId();
					bestQuality = i.quality;
				}
			}
			
		}
		id = bestID;
	}
	
	/**
	 * check whether this MetaXid might denote the same object
	 * 
	 * @param other
	 *            the other MetaXid to compare
	 * @return 0 - no indication for a match, 1 - some weak probability, 2 - probably a match, 3 -
	 *         surely matching
	 * @author gerry
	 * 
	 */
	
	public int match(MetaXid other){
		int ret = 0;
		for (Identity i : other.identities) {
			if (isMatching(i)) {
				if (i.isGUID) {
					return 3;
				} else if (i.quality == Xid.ASSIGNMENT_REGIONAL) {
					if (ret == 1) {
						ret = 2;
					} else {
						ret = 1;
					}
				} else if (i.quality == Xid.ASSIGNMENT_GLOBAL) {
					ret = 2;
				}
			}
		}
		return ret;
	}
	
	public Element toElement(Namespace ns){
		Element ret = new Element(XidElement.XMLNAME, ns);
		ret.setAttribute(XidElement.ATTR_ID, id);
		for (Identity i : identities) {
			Element ei = new Element(XidElement.ELEMENT_IDENTITY, ns);
			ei.setAttribute(XidElement.ATTR_IDENTITY_DOMAIN, i.domain);
			ei.setAttribute(XidElement.ATTR_IDENTITY_DOMAIN_ID, i.domainID);
			ei.setAttribute(XidElement.ATTR_IDENTITY_QUALITY, mapQuality(i.quality));
			ei.setAttribute(XidElement.ATTR_ISGUID, Boolean.toString(i.isGUID));
			ei.setAttribute(XidElement.ATTR_DATE, i.tt.toString(TimeTool.DATE_ISO));
			ret.addContent(ei);
		}
		return ret;
	}
	
	public boolean merge(MetaXid other){
		for (Identity i : other.identities) {
			Identity dom = findDomain(i.domain);
			if (dom == null) {
				identities.add(i);
			} else {
				if (i.domainID.equalsIgnoreCase(dom.domainID)) {
					continue;
				} else {
					if (i.tt.isBefore(dom.tt)) {
						i.domainID = dom.domainID;
						i.tt.set(dom.tt);
						i.quality = dom.quality;
						i.isGUID = dom.isGUID;
					}
				}
			}
		}
		return true;
	}
	
	private Identity findDomain(String domain){
		for (Identity i : identities) {
			if (i.domain.equalsIgnoreCase(domain)) {
				return i;
				
			}
		}
		return null;
	}
	
	private boolean isMatching(Identity i1){
		for (Identity i : identities) {
			if (i.domain.equalsIgnoreCase(i1.domain)) {
				if (i.domainID.equalsIgnoreCase(i1.domainID)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int mapQuality(String q){
		if (q.equalsIgnoreCase("local")) {
			return Xid.ASSIGNMENT_LOCAL;
		} else if (q.equalsIgnoreCase("regional")) {
			return Xid.ASSIGNMENT_REGIONAL;
		} else if (q.equalsIgnoreCase("global")) {
			return Xid.ASSIGNMENT_GLOBAL;
		}
		return -1;
	}
	
	private String mapQuality(int q){
		switch (q) {
		case Xid.ASSIGNMENT_GLOBAL:
			return "global";
		case Xid.ASSIGNMENT_LOCAL:
			return "local";
		case Xid.ASSIGNMENT_REGIONAL:
			return "regional";
		default:
			return "undefined";
		}
	}
	
	private class Identity {
		Identity(String d, String i, int q, boolean guid, String date){
			domain = d;
			domainID = i;
			quality = q;
			isGUID = guid;
			tt = new TimeTool(date);
		}
		
		TimeTool tt;
		String domain;
		String domainID;
		int quality;
		boolean isGUID;
	}
}
