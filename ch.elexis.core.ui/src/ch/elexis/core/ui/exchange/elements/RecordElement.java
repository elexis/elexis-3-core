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

import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.Record;
import ch.elexis.core.text.model.Samdas.XRef;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;
import ch.rgw.tools.XMLTool;

public class RecordElement extends XChangeElement {
	static final String ELEMENT_TEXT = "text";
	static final String ATTR_AUTHOR = "author";
	static final String ATTR_RESPONSIBLE = "responsible";
	static final String ATTR_DATE = "date";
	public static final String XMLNAME = "record";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public RecordElement asExporter(XChangeExporter c, Konsultation k){
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
					eText.addContent(st);
					getElement().addContent(eText);
					List<XRef> xrefs = record.getXrefs();
					for (XRef xref : xrefs) {
						MarkupElement me = new MarkupElement().asExporter(c, xref);
						add(me);
					}
				}
			}
		}
		c.getContainer().addMapping(this, k);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.getString("RecordElement.EntryDate")).append(getAttr(ATTR_DATE)).append(Messages.getString("RecordElement.CreatedBy")).append( //$NON-NLS-1$ //$NON-NLS-2$
				getAttr(ATTR_AUTHOR)).append("\n");
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
