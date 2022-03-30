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

import org.jdom.Element;

import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.Record;
import ch.elexis.core.text.model.Samdas.XRef;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;
import ch.rgw.tools.XMLTool;

public class RecordElement extends XChangeElement {
	static final String ELEMENT_TEXT = "text";
	static final String ELEMENT_EPISODE = "episode";
	static final String ATTR_AUTHOR = "author";
	static final String ATTR_RESPONSIBLE = "responsible";
	static final String ATTR_DATE = "date";
	public static final String XMLNAME = "record";

	public String getXMLName() {
		return XMLNAME;
	}

	public RecordElement asExporter(XChangeExporter c, Konsultation k) {
		asExporter(c);

		setAttribute(ATTR_DATE, new TimeTool(k.getDatum()).toString(TimeTool.DATE_ISO));
		Kontakt kMandant = k.getMandant();
		if (kMandant == null) {
			setAttribute(ATTR_RESPONSIBLE, "unknown");
		} else {
			ContactElement cMandant = c.addContact(kMandant);
			setAttribute(ATTR_RESPONSIBLE, cMandant.getID());
		}
		setAttribute(ATTR_ID, XMLTool.idToXMLID(k.getId()));
		c.getContainer().addChoice(this, k.getLabel(), k);
		VersionedResource vr = k.getEintrag();
		ResourceItem entry = vr.getVersion(vr.getHeadVersion());
		if (entry != null) {
			setAttribute(ATTR_AUTHOR, entry.remark);

			Samdas samdas = new Samdas(k.getEintrag().getHead());
			Record record = samdas.getRecord();
			if (record != null) {
				String st = record.getText();
				if (st != null) {
					Element eText = new Element(ELEMENT_TEXT, getContainer().getNamespace());
					eText.addContent(XMLTool.getValidXMLString(st));
					getElement().addContent(eText);
					List<XRef> xrefs = record.getXrefs();
					for (XRef xref : xrefs) {
						if (shouldAddXRef(xref)) {
							MarkupElement me = new MarkupElement().asExporter(c, xref);
							add(me);
						}
					}
				}
			}
		}
		c.getContainer().addMapping(this, k);
		return this;
	}

	private boolean shouldAddXRef(XRef xref) {
		// only add privatnotizen of current mandant
		if (xref.getProvider().toLowerCase().contains("privatnotizen")) {
			// return xref.getID().startsWith(CoreHub.actMandant.getId());
			return false;
		}
		return true;
	}

	public void addEpisodeRef(EpisodeElement episode) {
		Element eEpisode = new Element(ELEMENT_EPISODE, getContainer().getNamespace());
		eEpisode.setAttribute("ref", episode.getID());
		getElement().addContent(eEpisode);
	}

	public void addMeta(String name, String value) {
		MetaElement meta = new MetaElement().asExporter(sender, name, value);
		add(meta);
	}

	public MetaElement getMeta(String name) {
		@SuppressWarnings("unchecked")
		List<MetaElement> meta = (List<MetaElement>) getChildren(MetaElement.XMLNAME, MetaElement.class);
		if (meta != null && !meta.isEmpty()) {
			for (MetaElement metaElement : meta) {
				if (name.equals(metaElement.getAttr(MetaElement.ATTR_NAME))) {
					return metaElement;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.RecordElement_EntryDate).append(getAttr(ATTR_DATE)).append(Messages.RecordElement_CreatedBy)
				.append( // $NON-NLS-1$ //$NON-NLS-2$
						getAttr(ATTR_AUTHOR))
				.append("\n");
		List<Element> children = getElement().getChildren();
		if (children != null) {
			for (Element child : children) {
				if (child.getName().equals(ELEMENT_TEXT)) {
					continue;
				}
				sb.append(child.getName()).append(":\n");
				sb.append(child.getText()).append("\n");
			}
		}
		Element eText = getElement().getChild(ELEMENT_TEXT);
		if (eText != null) {
			String text = eText.getText();
			sb.append(text).append("\n------------------------------\n");
		}
		return sb.toString();
	}
}
