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

import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.LabItem;

public class FindingElement extends XChangeElement {
	public static final String ENCLOSING = "findings"; //$NON-NLS-1$
	public static final String XMLNAME = "finding"; //$NON-NLS-1$
	public static final String ATTR_NAME = "name"; //$NON-NLS-1$
	public static final String ATTR_NORMRANGE = "normRange"; //$NON-NLS-1$
	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
	public static final String ATTR_UNITS = "unit"; //$NON-NLS-1$
	public static final String ATTR_GROUP = "group"; //$NON-NLS-1$

	public static final String ELEMENT_XID = "xid"; //$NON-NLS-1$
	public static final String XIDBASE = "www.xid.ch/labitems/"; //$NON-NLS-1$

	public static final String TYPE_NUMERIC = "numeric"; //$NON-NLS-1$
	public static final String TYPE_TEXT = "text"; //$NON-NLS-1$
	public static final String TYPE_IMAGE = "image"; //$NON-NLS-1$
	public static final String TYPE_ABSOLUTE = "absolute"; //$NON-NLS-1$

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	FindingElement asExporter(XChangeExporter home, LabItem li) {
		asExporter(home);

		setAttribute(ATTR_NAME, li.getKuerzel());
		// TODO anpassen
		if (li.getTyp() == LabItemTyp.NUMERIC) {
			setAttribute(ATTR_TYPE, TYPE_NUMERIC);
			setAttribute(ATTR_NORMRANGE, li.getRefM()); // TODO anpassen
			setAttribute(ATTR_UNITS, li.getEinheit());

		} else if (li.getTyp() == LabItemTyp.ABSOLUTE) {
			setAttribute(ATTR_TYPE, TYPE_ABSOLUTE);
		} else if (li.getTyp() == LabItemTyp.TEXT) {
			setAttribute(ATTR_TYPE, TYPE_TEXT);
		}
		setAttribute(ATTR_GROUP, li.getGroup());
		XidElement eXid = new XidElement().asExporter(home, li);
		add(eXid);
		return this;
	}

}
