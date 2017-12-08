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

import ch.elexis.core.ui.exchange.XChangeExporter;

public class MetaElement extends XChangeElement {
	public static final String XMLNAME = "meta";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VALUE = "value";
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public MetaElement asExporter(XChangeExporter home, String name, String value){
		asExporter(home);
		setAttribute(ATTR_NAME, name);
		setAttribute(ATTR_VALUE, value);
		return this;
	}
	
}
