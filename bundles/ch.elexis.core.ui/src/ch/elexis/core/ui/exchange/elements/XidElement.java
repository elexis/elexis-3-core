/*******************************************************************************
 * Copyright (c) 2008-2011 by G. Weirich
 * This program is based on the Sgam-Exchange project,
 * (c) SGAM-Informatics
 * All rights resevred
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.exchange.elements;

import java.util.LinkedList;
import java.util.List;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.data.interfaces.ILabItem;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.IXid;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Artikel;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.Labor;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Xid;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.XMLTool;

public class XidElement extends XChangeElement {
	public static final String XMLNAME = "xid"; //$NON-NLS-1$
	public static final String ELEMENT_IDENTITY = "identity"; //$NON-NLS-1$
	public static final String ATTR_IDENTITY_DOMAIN = "domain"; //$NON-NLS-1$
	public static final String ATTR_IDENTITY_DOMAIN_ID = "domainID"; //$NON-NLS-1$
	public static final String ATTR_IDENTITY_QUALITY = "quality"; //$NON-NLS-1$
	public static final String ATTR_ISGUID = "isGUID"; //$NON-NLS-1$

	public static final String[] IDENTITY_QUALITIES = { "unknownAssignment", "localAssignment", "regionalAssignment", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"globalAssignment" }; //$NON-NLS-1$

	public enum XIDMATCH {
		NONE, POSSIBLE, SURE
	};

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public static boolean isUUID(IXid xid) {
		return (xid.getQuality() & 4) != 0;
	}

	public static int getPureQuality(IXid xid) {
		return (xid.getQuality() & 3);
	}

	public XidElement asExporter(XChangeExporter home, IVerrechenbar iv) {
		asExporter(home);
		if (iv instanceof PersistentObject) {
			PersistentObject po = (PersistentObject) iv;
			setAttribute(ATTR_ID, XMLTool.idToXMLID(po.getId()));
			addIdentities(po, iv.getXidDomain(), po.getId(), Xid.ASSIGNMENT_LOCAL, true);

		}
		return this;
	}

	public XidElement asExporter(XChangeExporter home, ILabItem li) {
		asExporter(home);
		setAttribute(ATTR_ID, XMLTool.idToXMLID(li.getId()));
		StringBuilder domainRoot = new StringBuilder(FindingElement.XIDBASE);
		Labor lab = ((LabItem) li).getLabor();
		if (lab == null || (!lab.isValid())) {
			domainRoot.append("unknown"); //$NON-NLS-1$
		} else {
			domainRoot.append(lab.get(Kontakt.FLD_NAME1));
		}
		String domain = domainRoot.toString().replace(";", "");
		String domainId = (li.getKuerzel() + "|" + li.getName()).replace(";", "");
		Xid.localRegisterXIDDomainIfNotExists(domain, domainId, Xid.ASSIGNMENT_LOCAL);
		addIdentities((LabItem) li, domain, domainId, Xid.ASSIGNMENT_LOCAL, true);
		return this;
	}

	public XidElement asExporter(XChangeExporter home, Artikel art) {
		asExporter(home);
		setAttribute(ATTR_ID, XMLTool.idToXMLID(art.getId()));
		String ean = art.getEAN();
		if (!StringTool.isNothing(ean)) {
			addIdentities(art, XidConstants.DOMAIN_EAN, ean, Xid.ASSIGNMENT_REGIONAL, false);
		}
		String pk = art.getPharmaCode();
		if (!StringTool.isNothing(pk)) {
			addIdentities(art, Artikel.XID_PHARMACODE, pk, Xid.ASSIGNMENT_REGIONAL, false);
		}
		return this;
	}

	public XidElement asExporter(XChangeExporter home, Kontakt k) {
		asExporter(home);
		IXid best = k.getXid();
		String id = XMLTool.idToXMLID(k.getId());
		if ((best.getQuality() & 7) >= Xid.QUALITY_GUID) {
			id = XMLTool.idToXMLID(best.getDomainId());
		} else {
			k.addXid(XidConstants.DOMAIN_ELEXIS, id, true);
		}
		setAttribute(ATTR_ID, XMLTool.idToXMLID(k.getId()));
		List<IXid> xids = k.getXids();
		for (IXid xid : xids) {
			int val = xid.getQuality();
			int v1 = val & 3;
			Identity ident = new Identity().asExporter(home, xid.getDomain(), xid.getDomainId(), v1, isUUID(xid));
			add(ident);
		}
		return this;
	}

	public XidElement asExporter(XChangeExporter home, Fall fall) {
		asExporter(home);
		IXid best = fall.getXid();
		String id = XMLTool.idToXMLID(fall.getId());
		if ((best.getQuality() & 7) >= Xid.QUALITY_GUID) {
			id = XMLTool.idToXMLID(best.getDomainId());
		} else {
			fall.addXid(XidConstants.DOMAIN_ELEXIS, id, true);
		}
		setAttribute(ATTR_ID, XMLTool.idToXMLID(fall.getId()));
		List<IXid> xids = fall.getXids();
		for (IXid xid : xids) {
			int val = xid.getQuality();
			int v1 = val & 3;
			Identity ident = new Identity().asExporter(home, xid.getDomain(), xid.getDomainId(), v1, isUUID(xid));
			add(ident);
		}
		return this;
	}

	private void addIdentities(PersistentObject po, String domain, String domid, int q, boolean bGuid) {
		List<IXid> xids = po.getXids();

		boolean bDomain = false;
		// XChangeContainer home = getContainer();
		for (IXid xid : xids) {
			if (xid.getDomain().equals(domain)) {
				bDomain = true;
			}
			Identity ident = new Identity().asExporter(sender, xid.getDomain(), xid.getDomainId(), getPureQuality(xid),
					isUUID(xid));
			add(ident);
		}
		if (bDomain == false) {
			po.addXid(domain, domid, false);
			Identity ident = new Identity().asExporter(sender, domain, domid, q, bGuid);
			add(ident);
		}
	}

	public void addIdentity(String domain, String domainID, int quality, boolean isGuid) {
		add(new Identity().asExporter(sender, domain, domainID, quality, isGuid));
	}

	@SuppressWarnings("unchecked")
	public List<Identity> getIdentities() {
		return (List<Identity>) getChildren(ELEMENT_IDENTITY, Identity.class);
	}

	public void setMainID(String domain) {
		Identity best = null;
		for (Identity cand : getIdentities()) {
			if (domain != null) {
				if (cand.getAttr(ATTR_IDENTITY_DOMAIN).equalsIgnoreCase(domain)) {
					best = cand;
					break;
				}
			} else {
				if (best == null) {
					best = cand;
				} else {

					if (best.isGuid()) {
						if (cand.isGuid()) {
							if (cand.getQuality() > best.getQuality()) {
								best = cand;
							}
						}
					} else {
						if (cand.isGuid()) {
							best = cand;
						} else {
							if (cand.getQuality() > best.getQuality()) {
								best = cand;
							}
						}
					}

				}

			}
		}
		if (best == null || (!best.isGuid())) {
			best = new Identity().asExporter(sender, XidConstants.DOMAIN_ELEXIS, ElexisIdGenerator.generateId(),
					Xid.ASSIGNMENT_LOCAL, true);
			add(best);
		}
		setAttribute(ATTR_ID, XMLTool.idToXMLID(best.getAttr(ATTR_IDENTITY_DOMAIN_ID)));
	}

	/**
	 * Compare this XID -Element with the xids of a PersistentObject
	 *
	 * @param po a PersistentObject to match
	 * @return XIDMATCH.SURE if both xids match in one or more identities of GUID
	 *         quality or in two or more identities without GUID quality but more
	 *         than local assignment.<br/>
	 *         XIDMATCH.POSSIBLE if the xids match in one identity without GUID
	 *         quality XIDMATCH.NONE otherwise.
	 */
	@SuppressWarnings("unchecked")
	public XIDMATCH match(PersistentObject po) {
		if (po.getId().equals(getAttr(ATTR_ID))) {
			return XIDMATCH.SURE;
		}
		int sure = 0;
		List<IXid> poXids = po.getXids();
		List<Identity> idents = (List<Identity>) getChildren(ELEMENT_IDENTITY, Identity.class);
		for (IXid xid : poXids) {
			String domain = xid.getDomain();
			String domid = xid.getDomainId();
			for (Identity ident : idents) {
				if (ident.getAttr(ATTR_IDENTITY_DOMAIN).equals(domain)
						&& ident.getAttr(ATTR_IDENTITY_DOMAIN_ID).equals(domid)) {
					if (XidElement.isUUID(xid)) {
						return XIDMATCH.SURE;
					} else {
						if (xid.getQuality() > Xid.ASSIGNMENT_LOCAL) {
							sure++;
						}
					}
				}
			}
		}
		switch (sure) {
		case 0:
			return XIDMATCH.NONE;
		case 1:
			return XIDMATCH.POSSIBLE;
		default:
			return XIDMATCH.SURE;
		}

	}

	/**
	 * Find the Object(s) possibly matching this Xid-Element
	 *
	 * @return a List with matching objects that might be empty but will not be
	 *         null.
	 */
	@SuppressWarnings("unchecked")
	public List<IPersistentObject> findObject() {
		List<Identity> idents = (List<Identity>) getChildren(ELEMENT_IDENTITY, Identity.class);
		List<IPersistentObject> candidates = new LinkedList<>();
		boolean lastGuid = false;
		int lastQuality = 0;
		for (XChangeElement ident : idents) {
			String domain = ident.getAttr(ATTR_IDENTITY_DOMAIN);
			String domain_id = ident.getAttr(ATTR_IDENTITY_DOMAIN_ID);
			String quality = ident.getAttr(ATTR_IDENTITY_QUALITY);
			String isguid = ident.getAttr(XidElement.ATTR_ISGUID);
			IPersistentObject cand = Xid.findObject(domain, domain_id);
			if (cand != null) {
				boolean actGuid = Boolean.parseBoolean(isguid);
				int actQuality = StringTool.getIndex(IDENTITY_QUALITIES, quality);
				if (candidates.contains(cand)) {
					if ((lastGuid == true) && (actGuid == false)) {
						continue;
					}
					if (actQuality < lastQuality) {
						continue;
					}
					candidates.remove(cand);
				}
				candidates.add(cand);
				lastQuality = actQuality;
				lastGuid = actGuid;
			}
		}

		return candidates;
	}

	public static class Identity extends XChangeElement {

		public Identity() {

		}

		@Override
		public String getXMLName() {
			return ELEMENT_IDENTITY;
		}

		public Identity asExporter(XChangeExporter home, String domain, String domain_id, int quality, boolean isGuid) {
			asExporter(home);
			setAttribute(ATTR_IDENTITY_DOMAIN, domain);
			setAttribute(ATTR_IDENTITY_DOMAIN_ID, domain_id);
			setAttribute(ATTR_IDENTITY_QUALITY, IDENTITY_QUALITIES[quality]);
			setAttribute(ATTR_ISGUID, Boolean.toString(isGuid));
			return this;
		}

		public String getDomain() {
			return getAttr(ATTR_IDENTITY_DOMAIN);
		}

		public String getDomainId() {
			return getAttr(ATTR_IDENTITY_DOMAIN_ID);
		}

		public int getQuality() {
			String sq = getAttr(ATTR_IDENTITY_QUALITY);
			int idx = StringTool.getIndex(IDENTITY_QUALITIES, sq);
			return idx > 0 ? idx : 0;
		}

		public boolean isGuid() {
			return Boolean.parseBoolean(getAttr(ATTR_ISGUID));
		}
	}

}
