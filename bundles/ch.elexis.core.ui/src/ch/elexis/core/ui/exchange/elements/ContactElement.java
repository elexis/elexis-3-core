/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.exchange.elements;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.interfaces.ISticker;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Sticker;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * A Contact can contain elements of the types address, connection, and medical
 *
 * @author Gerry
 *
 */
public class ContactElement extends XChangeElement {
	public static final String XMLNAME = "contact"; //$NON-NLS-1$
	public static final String ATTR_BIRTHDATE = "birthdate"; //$NON-NLS-1$
	public static final String ATTR_FIRSTNAME = "firstname"; //$NON-NLS-1$
	public static final String ATTR_MIDDLENAME = "middlename"; //$NON-NLS-1$
	public static final String ATTR_LASTNAME = "lastname"; //$NON-NLS-1$
	public static final String ATTR_SEX = "sex"; //$NON-NLS-1$
	public static final String ATTR_SALUTATION = "salutation"; //$NON-NLS-1$
	public static final String ATTR_TITLE = "title"; //$NON-NLS-1$
	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
	public static final String ATTR_SHORTNAME = "shortname"; //$NON-NLS-1$
	public static final String ELEM_XID = "xid"; //$NON-NLS-1$
	public static final String ELEM_ADDRESS = "address"; //$NON-NLS-1$
	public static final String VALUE_PERSON = "person"; //$NON-NLS-1$
	public static final String VALUE_ORGANIZATION = "organization"; //$NON-NLS-1$
	public static final String VALUE_MALE = "male"; //$NON-NLS-1$
	public static final String VALUE_FEMALE = "female"; //$NON-NLS-1$
	public static final String ATTR_REMARK = "remark"; //$NON-NLS-1$

	public void add(AddressElement ae) {
		super.add(ae);
	}

	public void add(ContactRefElement ce) {
		super.add(ce);
	}

	public void add(MedicalElement me) {
		super.add(me);
	}

	public List<AddressElement> getAddresses() {
		return (List<AddressElement>) getChildren(ELEM_ADDRESS, AddressElement.class);
	}

	public ContactElement asExporter(XChangeExporter parent, Kontakt k) {
		asExporter(parent);
		XidElement eXid = new XidElement().asExporter(parent, k);
		add(eXid);
		if (k.istPerson()) {
			Person p = Person.load(k.getId());
			setAttribute(ATTR_TYPE, VALUE_PERSON);
			setAttribute(ATTR_LASTNAME, p.getName());
			setAttribute(ATTR_FIRSTNAME, p.getVorname());
			if (p.getGeschlecht().equals(Person.MALE)) { // $NON-NLS-1$
				setAttribute(ATTR_SEX, VALUE_MALE);
			} else {
				setAttribute(ATTR_SEX, VALUE_FEMALE);
			}
			String gebdat = p.getGeburtsdatum();
			if (!StringTool.isNothing(gebdat)) {
				setAttribute(ATTR_BIRTHDATE, new TimeTool(gebdat).toString(TimeTool.DATE_ISO));
			}

		} else {
			setAttribute(ATTR_TYPE, VALUE_ORGANIZATION);
			String name = k.get(Kontakt.FLD_NAME1);
			if (StringUtils.isEmpty(name)) {
				name = k.getLabel(false);
			}
			setAttribute(ATTR_LASTNAME, name);
		}
		String bemerkung = k.getBemerkung();
		if (!StringTool.isNothing(bemerkung)) {
			setAttribute(ATTR_REMARK, bemerkung);
		}
		add(new AddressElement().asExporter(parent, k.getAnschrift(), "default")); //$NON-NLS-1$
		List<ISticker> stickers = k.getStickers();
		addStickersMeta(stickers);
		parent.getContainer().addMapping(this, k);
		return this;
	}

	public List<ContactRefElement> getAssociations() {
		List<ContactRefElement> ret = (List<ContactRefElement>) getChildren("connection", ContactRefElement.class); //$NON-NLS-1$
		return ret;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.ContactElement_Name).append(getAttr(ATTR_LASTNAME)).append(StringTool.lf); // $NON-NLS-1$
		sb.append(Messages.ContactElement_vorname).append(getAttr(ATTR_FIRSTNAME)); // $NON-NLS-1$
		String middle = getAttr(ATTR_MIDDLENAME);
		if (middle.length() > 0) {
			sb.append(StringTool.space).append(middle);
		}
		sb.append(Messages.ContactElement_gebdat); // $NON-NLS-1$
		TimeTool geb = new TimeTool(getAttr(ATTR_BIRTHDATE));
		sb.append(geb.toString(TimeTool.DATE_GER)).append(StringTool.lf);
		sb.append("PID: ").append(getAttr(ATTR_ID)).append(StringTool.lf + StringTool.lf); //$NON-NLS-1$
		List<AddressElement> addresses = getAddresses();
		for (AddressElement adr : addresses) {
			sb.append(adr.toString()).append(StringTool.lf);
		}
		return sb.toString();
	}

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public PersistentObject doImport(PersistentObject context) {
		XidElement eXid = getXid();
		Kontakt ret = null;
		if (eXid != null) {
			List<IPersistentObject> cands = eXid.findObject();
			if (cands.isEmpty()) {
				AddressElement ae = null;
				List<AddressElement> lae = getAddresses();
				if (!lae.isEmpty()) {
					if (lae.size() == 1) {
						ae = lae.get(0);
					} else {
						for (AddressElement adr : lae) {
							if (adr.getAttr(AddressElement.ATTR_DESCRIPTION)
									.equalsIgnoreCase(AddressElement.VALUE_DEFAULT)) {
								ae = adr;
								break;
							}
						}
						if (ae == null) {
							ae = lae.get(0);
						}
					}

				}
				String strasse = null;
				String plz = null;
				String ort = null;
				String natel = null;
				if (ae != null) {
					strasse = ae.getAttr(AddressElement.ATTR_STREET);
					plz = ae.getAttr(AddressElement.ATTR_ZIP);
					ort = ae.getAttr(AddressElement.ATTR_CITY);
				}
				if (getAttr(ATTR_TYPE).equalsIgnoreCase(VALUE_PERSON)) {
					String s = getAttr(ATTR_SEX).equals(VALUE_MALE) ? Person.MALE : Person.FEMALE;
					ret = KontaktMatcher.findPerson(getAttr(ATTR_LASTNAME), getAttr(ATTR_FIRSTNAME),
							getAttr(ATTR_BIRTHDATE), s, strasse, plz, ort, natel, KontaktMatcher.CreateMode.CREATE);

				} else {
					ret = KontaktMatcher.findOrganisation(getAttr(ATTR_LASTNAME), getAttr(ATTR_FIRSTNAME), strasse, plz,
							ort, KontaktMatcher.CreateMode.CREATE);
				}
			} else if (cands.size() == 1) {
				if (getAttr(ATTR_TYPE).equalsIgnoreCase(VALUE_PERSON)) {
					ret = Person.load(cands.get(0).getId());
				} else {
					ret = Organisation.load(cands.get(0).getId());
				}
			}
			getStickersMeta(ret);
			MedicalElement me = (MedicalElement) getChild(MedicalElement.XMLNAME, MedicalElement.class);
			me.doImport(ret);
		}
		return ret;
	}

	protected void getStickersMeta(Kontakt ret) {
		List<MetaElement> meta = (List<MetaElement>) getChildren(MetaElement.XMLNAME, MetaElement.class);
		if (meta != null && !meta.isEmpty()) {
			for (MetaElement metaElement : meta) {
				if ("stickers".equals(metaElement.getAttr(MetaElement.ATTR_NAME))) { //$NON-NLS-1$
					String stickersString = metaElement.getAttr(MetaElement.ATTR_VALUE);
					if (stickersString != null) {
						String[] stickerStrings = stickersString.split("\\|"); //$NON-NLS-1$
						for (String stickerString : stickerStrings) {
							String[] stickerParts = stickerString.split("::"); //$NON-NLS-1$
							if (stickerParts != null && stickerParts.length > 1
									&& StringUtils.isNotBlank(stickerParts[0])) {
								Sticker sticker = getOrCreateSticker(stickerParts);
								if (sticker != null) {
									ret.addSticker(sticker);
								}
							}
						}
					}
				}
			}
		}
	}

	private Sticker getOrCreateSticker(String[] stickerParts) {
		Query<Sticker> query = new Query<>(Sticker.class);
		query.add(Sticker.FLD_NAME, Query.EQUALS, stickerParts[0]);
		List<Sticker> existing = query.execute();
		if (existing.isEmpty()) {
			Sticker ret = new Sticker(stickerParts[0], "000000", "ffffff"); //$NON-NLS-1$ //$NON-NLS-2$
			if (stickerParts.length > 1 && StringUtils.isNotBlank(stickerParts[1])) {
				ret.set(Sticker.FLD_VALUE, stickerParts[1]);
			}
			if (stickerParts.length > 2 && StringUtils.isNotBlank(stickerParts[2])) {
				ret.set(Sticker.FLD_FOREGROUND, stickerParts[2]);
			}
			if (stickerParts.length > 3 && StringUtils.isNotBlank(stickerParts[3])) {
				ret.set(Sticker.FLD_BACKGROUND, stickerParts[3]);
			}
			return ret;
		} else {
			return existing.get(0);
		}
	}

	private void addStickersMeta(List<ISticker> stickers) {
		if (stickers != null && !stickers.isEmpty()) {
			StringJoiner sj = new StringJoiner("|"); //$NON-NLS-1$
			stickers.stream().forEach(s -> sj
					.add(s.getLabel() + "::" + s.getWert() + "::" + s.getForeground() + "::" + s.getBackground())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			addMeta("stickers", sj.toString()); //$NON-NLS-1$
		}
	}

	public void addMeta(String name, String value) {
		MetaElement meta = new MetaElement().asExporter(sender, name, value);
		add(meta);
	}
}
