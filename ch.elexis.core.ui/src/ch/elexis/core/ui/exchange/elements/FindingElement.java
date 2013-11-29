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

import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.LabItem;

public class FindingElement extends XChangeElement {
	public static final String ENCLOSING = "findings";
	public static final String XMLNAME = "finding";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_NORMRANGE = "normRange";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_UNITS = "unit";
	public static final String ATTR_GROUP = "group";
	
	public static final String ELEMENT_XID = "xid";
	public static final String XIDBASE = "www.xid.ch/labitems/";
	
	public static final String TYPE_NUMERIC = "numeric";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_ABSOLUTE = "absolute";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	FindingElement asExporter(XChangeExporter home, LabItem li){
		asExporter(home);
		
		setAttribute(ATTR_NAME, li.getKuerzel());
		if (li.getTyp().equals(LabItem.typ.NUMERIC)) {
			setAttribute(ATTR_TYPE, TYPE_NUMERIC);
			setAttribute(ATTR_NORMRANGE, li.getRefM()); // TODO anpassen
			setAttribute(ATTR_UNITS, li.getEinheit());
			
		} else if (li.getTyp().equals(LabItem.typ.ABSOLUTE)) {
			setAttribute(ATTR_TYPE, TYPE_ABSOLUTE);
		} else if (li.getTyp().equals(LabItem.typ.TEXT)) {
			setAttribute(ATTR_TYPE, TYPE_TEXT);
		}
		setAttribute(ATTR_GROUP, li.getGroup());
		XidElement eXid = new XidElement().asExporter(home, li);
		add(eXid);
		return this;
	}
	
}
