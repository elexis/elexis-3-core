/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
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

public class RiskElement extends XChangeElement {
	public static final String XMLNAME = "risk"; //$NON-NLS-1$
	public static final String ATTR_CONFIRMEDBY = "confirmedBy"; //$NON-NLS-1$
	public static final String ATTR_FIRSTMENTIONED = "firstMentioned"; //$NON-NLS-1$
	public static final String ATTR_SUBSTANCE = "substance"; //$NON-NLS-1$
	public static final String ATTR_RELEVANCE = "relevance"; //$NON-NLS-1$
	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
	public static String ELEMENT_META = "meta"; //$NON-NLS-1$

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public RiskElement asExporter(XChangeExporter parent, String name) {
		return asExporter(parent, name, null);
	}

	/**
	 *
	 * @param parent
	 * @param name
	 * @param type   a specific risk type. e.g. "allergy"
	 * @return
	 * @since 3.7
	 */
	public RiskElement asExporter(XChangeExporter parent, String name, String type) {
		asExporter(parent);
		setAttribute(ATTR_SUBSTANCE, name);
		if (type != null) {
			setAttribute(ATTR_TYPE, type);
		}
		parent.getContainer().addChoice(this, name);
		return this;
	}
}
