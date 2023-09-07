/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich, SGAM.informatics and Elexis
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
import ch.elexis.data.Anschrift;
import ch.rgw.tools.StringTool;

public class AddressElement extends XChangeElement {

	public static final String XMLNAME = "address"; //$NON-NLS-1$
	public static final String ATTR_STREET = "street"; //$NON-NLS-1$
	public static final String ATTR_ZIP = "zip"; //$NON-NLS-1$
	public static final String ATTR_CITY = "city"; //$NON-NLS-1$
	public static final String ATTR_COUNTRY = "country"; //$NON-NLS-1$
	public static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String VALUE_DEFAULT = "default"; //$NON-NLS-1$

	public AddressElement asExporter(XChangeExporter parent, Anschrift an, String bezug) {
		asExporter(parent);
		setAnschrift(an);
		setBezug(bezug);
		return this;
	}

	public void setAnschrift(Anschrift an) {
		setAttribute(ATTR_STREET, an.getStrasse());
		setAttribute(ATTR_ZIP, an.getPlz());
		setAttribute(ATTR_CITY, an.getOrt());
		setAttribute(ATTR_COUNTRY, an.getLand());
	}

	public void setBezug(String bezug) {
		setAttribute(ATTR_DESCRIPTION, bezug);
	}

	public String getBezug() {
		return getAttr(ATTR_DESCRIPTION);
	}

	public Anschrift getAnschrift() {
		Anschrift ret = new Anschrift();
		ret.setLand(getAttr(ATTR_COUNTRY));
		ret.setOrt(getAttr(ATTR_CITY));
		ret.setPlz(getAttr(ATTR_ZIP));
		ret.setStrasse(getAttr(ATTR_STREET));
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(getAttr(ATTR_STREET)).append(", ").append(getAttr(ATTR_ZIP)).append(StringTool.space) //$NON-NLS-1$
				.append(getAttr(ATTR_CITY)).append(StringTool.space).append(getAttr(ATTR_COUNTRY));
		return ret.toString();
	}

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

}
