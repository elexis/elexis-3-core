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

import ch.elexis.core.data.LabResult;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

public class ResultElement extends XChangeElement {
	public static final String XMLNAME = "result";
	public static final String ATTR_DATE = "timestamp";
	public static final String ATTR_NORMAL = "isNormal";
	public static final String ATTR_LABITEM = "findingRef";
	public static final String ELEMENT_META = "meta";
	public static final String ATTRIB_CREATOR = "creator";
	public static final String ELEMENT_IMAGE = "image";
	public static final String ELEMENT_TEXTRESULT = "textResult";
	public static final String ELEMENT_DOCRESULT = "documentRef";
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
	public static ResultElement addResult(MedicalElement me, LabResult lr){
		List<FindingElement> findings = me.getAnalyses();
		for (FindingElement fe : findings) {
			if (fe.getXid().getID().equals(XMLTool.idToXMLID(lr.getItem().getId()))) {
				ResultElement re = new ResultElement().asExporter(me.sender, lr);
				me.addAnalyse(re);
				return re;
			}
		}
		FindingElement fe = new FindingElement().asExporter(me.sender, lr.getItem());
		me.addFindingItem(fe);
		ResultElement re = new ResultElement().asExporter(me.sender, lr);
		me.addAnalyse(re);
		return re;
	}
	
	private ResultElement asExporter(XChangeExporter home, LabResult lr){
		asExporter(home);
		setAttribute("id", XMLTool.idToXMLID(lr.getId()));
		setAttribute(ATTR_DATE, new TimeTool(lr.getDate()).toString(TimeTool.DATETIME_XML));
		setAttribute(ATTR_LABITEM, XMLTool.idToXMLID(lr.getItem().getId()));
		ResultElement eResult = new ResultElement();
		eResult.setText(lr.getResult());
		add(eResult);
		// setAttribute(ATTR_NORMAL,); // TODO
		home.getContainer().addChoice(this, lr.getLabel(), lr);
		return this;
	}
	
	public void setText(String text){
		
	}
}
