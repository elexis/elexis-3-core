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

import ch.elexis.core.text.model.Samdas.XRef;
import ch.elexis.core.ui.exchange.XChangeExporter;

public class MarkupElement extends XChangeElement {
	public static final String XMLNAME = "markup";
	public static final String ATTR_POS = "pos";
	public static final String ATTR_LEN = "length";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_TEXT = "text";
	public static final String ATTRIB_HINT = "hint";
	public static final String ELEME_META = "meta";
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public MarkupElement asExporter(XChangeExporter home, XRef xref){
		asExporter(home);
		setAttribute(ATTR_POS, Integer.toString(xref.getPos()));
		setAttribute(ATTR_LEN, Integer.toString(xref.getLength()));
		setAttribute(ATTR_TYPE, xref.getProvider());
		add(new MetaElement().asExporter(home, ATTR_ID, xref.getID()));
		add(new MetaElement().asExporter(home, "provider", xref.getProvider()));
		return this;
	}
}
