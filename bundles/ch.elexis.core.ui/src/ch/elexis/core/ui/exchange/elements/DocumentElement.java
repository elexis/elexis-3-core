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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

public class DocumentElement extends XChangeElement {
	public static final String XMLNAME = "document";
	public static final String ATTR_TITLE = "title";
	public static final String ATTR_ORIGIN = "origin";
	public static final String ATTR_DESTINATION = "destination";
	public static final String ATTR_MIMETYPE = "mimetype";
	public static final String ATTR_SUBJECT = "subject";
	public static final String ATTR_PLACEMENT = "placement";
	public static final String ATTR_DATE = "date";
	public static final String ATTR_RECORDREF = "recordref";
	
	public static final String ATTR_DOCUMENT_TYPE = "doctype";
	public static final String VAL_DOCUMENT_TYPE_LETTER = "letter";
	public static final String VAL_DOCUMENT_TYPE_OMNIVORE = "omnivore";
	
	public static final String ELEMENT_XID = "xid";
	public static final String ELEMENT_HINT = "hint";
	public static final String ELEMENT_CONTENTS = "contents";
	
	public static final String PLACEMENT_INLINE = "inline";
	public static final String PLACEMENT_INFILE = "infile";
	public static final String PLACEMENT_URL = "url";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public DocumentElement asExporter(XChangeExporter parent, Brief b, String documentType){
		asExporter(parent, b.getMimeType(), b.getId(), b.loadBinary(), documentType, b.getBetreff(), b.getDatum());
		
		setDestination(b.getAdressat());
		setOriginator(Kontakt.load(b.get("AbsenderID")));
		
		String idex = b.get("BehandlungsID");
		if (idex != null) {
			setAttribute(ATTR_RECORDREF, XMLTool.idToXMLID(idex));
		}
		setHint("Dies ist ein Dokument im OpenDocument-Format. Sie können es zum Beispiel mit OpenOffice (http://www.openoffice.org) lesen");
		parent.getContainer().addChoice(this, b.getLabel(), b);
		return this;
	}
	
	public DocumentElement asExporter(XChangeExporter parent, IDocument iDocument,
		String documentType){
		byte[] content = null;
		try (InputStream is = iDocument.getContent()) {
			content = IOUtils.toByteArray(is);
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass())
				.warn(iDocument.getId() + " Error serializing to byte array", e);
		}
		
		TimeTool created = new TimeTool(iDocument.getCreated());
		asExporter(parent, iDocument.getMimeType(), iDocument.getId(), content, documentType,
			iDocument.getTitle(), created.toString(TimeTool.DATE_GER));
		if (iDocument.getAuthor() != null) {
			setAttribute(ATTR_ORIGIN, iDocument.getAuthor().getId());
		}
		parent.getContainer().addChoice(this, iDocument.getLabel(), iDocument);
		return this;
	}
	
	private DocumentElement asExporter(XChangeExporter parent, String mimetype, String id,
		byte[] binary, String documentType, String title, String date){
		asExporter(parent);
		setAttribute(ATTR_MIMETYPE, mimetype);
		setDefaultXid(id);
		setAttribute(ATTR_PLACEMENT, PLACEMENT_INFILE);
		parent.addBinary(getID(), binary);
		setAttribute(ATTR_DOCUMENT_TYPE, documentType);
		setTitle(title);
		setDate(date);
		
		return this;
	}

	public void setTitle(String title){
		setAttribute(ATTR_TITLE, title);
	}
	
	public void setOriginator(Kontakt k){
		if (k != null && k.isValid()) {
			ContactElement ce = sender.addContact(k);
			setAttribute(ATTR_ORIGIN, ce.getID());
		}
	}
	
	public void setDestination(Kontakt k){
		if (k != null && k.isValid()) {
			ContactElement ce = sender.addContact(k);
			setAttribute(ATTR_DESTINATION, ce.getID());
		}
	}
	
	public void addMeta(String name, String value){
		MetaElement meta = new MetaElement().asExporter(sender, name, value);
		add(meta);
	}
	
	public MetaElement getMeta(String name){
		@SuppressWarnings("unchecked")
		List<MetaElement> meta =
			(List<MetaElement>) getChildren(MetaElement.XMLNAME, MetaElement.class);
		if (meta != null && !meta.isEmpty()) {
			for (MetaElement metaElement : meta) {
				if (name.equals(metaElement.getAttr(MetaElement.ATTR_NAME))) {
					return metaElement;
				}
			}
		}
		return null;
	}
	
	public void setDate(String date){
		TimeTool tt = new TimeTool(date);
		setAttribute(ATTR_DATE, tt.toString(TimeTool.DATE_ISO));
	}
	
	public void setHint(String hint){
		Element eHint = new Element(ELEMENT_HINT, getContainer().getNamespace());
		eHint.setText(hint);
		getElement().addContent(eHint);
	}
	
	public void setSubject(String subject){
		setAttribute(ATTR_SUBJECT, subject);
	}
	
	public void setMimetype(String desc){
		setAttribute(ATTR_MIMETYPE, desc);
	}
}
