/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
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

import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.XRef;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Konsultation;
import ch.elexis.data.NamedBlob2;

public class MarkupElement extends XChangeElement {
	public static final String XMLNAME = "markup"; //$NON-NLS-1$
	public static final String ATTR_POS = "pos"; //$NON-NLS-1$
	public static final String ATTR_LEN = "length"; //$NON-NLS-1$
	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
	public static final String ATTR_TEXT = "text"; //$NON-NLS-1$
	public static final String ATTRIB_HINT = "hint"; //$NON-NLS-1$
	public static final String ELEME_META = "meta"; //$NON-NLS-1$

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public MarkupElement asExporter(XChangeExporter home, XRef xref) {
		asExporter(home);
		setAttribute(ATTR_POS, Integer.toString(xref.getPos()));
		setAttribute(ATTR_LEN, Integer.toString(xref.getLength()));
		setAttribute(ATTR_TYPE, xref.getProvider());
		addMeta(ATTR_ID, xref.getID());
		addMeta("provider", xref.getProvider()); //$NON-NLS-1$
		if (shouldAddContent(xref)) {
			addContent(home, xref);
		}
		return this;
	}

	private void addContent(XChangeExporter home, XRef xref) {
		if (xref.getProvider().toLowerCase().contains("privatnotizen")) { //$NON-NLS-1$
			NamedBlob2 contentBlob = NamedBlob2.load(xref.getID());
			if (contentBlob != null && contentBlob.exists()) {
				addMeta("content", contentBlob.getString()); //$NON-NLS-1$
			}
		}
	}

	private boolean shouldAddContent(XRef xref) {
		return xref.getProvider().toLowerCase().contains("privatnotizen"); //$NON-NLS-1$
	}

	public void doImport(Konsultation kons) {
		if (getMeta("content") != null) { //$NON-NLS-1$
			if (getMeta("provider") != null) { //$NON-NLS-1$
				String provider = getMeta("provider").getAttr(ATTR_VALUE); //$NON-NLS-1$
				importContent(kons, provider, getMeta(ATTR_ID), getMeta("content")); //$NON-NLS-1$
			}
		}
	}

	private void importContent(Konsultation kons, String provider, MetaElement metaId, MetaElement metaContent) {
		String id = metaId.getAttr(ATTR_VALUE);
		String content = metaContent.getAttr(ATTR_VALUE);
		Integer pos = Integer.parseInt(getAttr(ATTR_POS));
		Integer length = Integer.parseInt(getAttr(ATTR_LEN));
		if (provider.toLowerCase().contains("privatnotizen") && id.contains(":")) { //$NON-NLS-1$ //$NON-NLS-2$
			// reset id
			String[] idparts = id.split(":"); //$NON-NLS-1$
			if (idparts.length > 1) {
				id = kons.getMandant().getId() + ":" + idparts[1]; //$NON-NLS-1$
			}
			NamedBlob2 contentBlob = NamedBlob2.create(id, false);
			contentBlob.putString(content);
			Samdas samdas = new Samdas(kons.getEintrag().getHead());
			Samdas.Record record = samdas.getRecord();
			Samdas.XRef xref = new Samdas.XRef(provider, id, pos, length);
			record.add(xref);
			kons.updateEintrag(samdas.toString(), true);
		}
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
}
