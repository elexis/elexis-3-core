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

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.core.ui.exchange.elements.XidElement.Identity;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

public class MedicationElement extends XChangeElement {
	public static final String XMLNAME = "medication";
	public static final String ATTRIB_BEGINDATE = "startDate";
	public static final String ATTRIB_ENDDATE = "stopDate";
	public static final String ATTRIB_PRODUCT = "product";
	public static final String ATTRIB_DOSAGE = "dosage";
	public static final String ATTRIB_UNITS = "dosageUnit";
	public static final String ATTRIB_FREQUENCY = "frequency";
	public static final String ATTRIB_SUBSTANCE = "substance";
	public static final String ATTRIB_REMARK = "remark";
	public static final String ATTRIB_TYPE = "type";
	public static final String ELEMENT_XID = "xid";
	public static final String ELEMENT_META = "meta";
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public MedicationElement asExporter(XChangeExporter parent, Prescription pr){
		asExporter(parent);
		Artikel art = pr.getArtikel();
		String begin = pr.getBeginDate();
		String end = pr.getEndDate();
		String dose = pr.getDosis();
		String remark = pr.getBemerkung();
		setAttribute(ATTRIB_BEGINDATE, XMLTool.dateToXmlDate(begin));
		if (!StringTool.isNothing(end)) {
			setAttribute(ATTRIB_ENDDATE, XMLTool.dateToXmlDate(end));
		}
		setAttribute(ATTRIB_FREQUENCY, dose);
		setAttribute(ATTRIB_PRODUCT, art.getLabel());
		setAttribute(ATTRIB_REMARK, remark);
		setAttribute(ATTRIB_TYPE, pr.getEntryType().name());
		add(new XidElement().asExporter(parent, art));
		parent.getContainer().addChoice(this, pr.getLabel(), pr);
		return this;
	}
	
	public String getFirstDate(){
		String begin = getAttr(ATTRIB_BEGINDATE);
		return new TimeTool(begin).toString(TimeTool.DATE_GER);
	}
	
	public String getLastDate(){
		String last = getAttr(ATTRIB_ENDDATE);
		if(StringUtils.isBlank(last)) {
			return null;
		}
		return new TimeTool(last).toString(TimeTool.DATE_GER);
	}
	
	public String getText(){
		return getAttr(ATTRIB_SUBSTANCE);
	}
	
	public String getDosage(){
		return getAttr(ATTRIB_FREQUENCY);
	}
	
	public String getSubstance(){
		return getAttr(ATTRIB_SUBSTANCE);
	}
	
	public String getRemark() {
		return getAttr(ATTRIB_REMARK);
	}
	
	public String getProduct() {
		return getAttr(ATTRIB_PRODUCT);
	}
	
	/**
	 * @return the GTIN if found or <code>null</code>
	 */
	public String getGtin(){
		return getXid().getIdentities().stream()
			.filter(i -> "www.xid.ch/id/ean".equalsIgnoreCase(i.getDomain())).findFirst()
			.map(Identity::getDomainId).orElse(null);
	}
	
	public String getPharmacode() {
		return getXid().getIdentities().stream()
				.filter(i -> "www.xid.ch/id/pharmacode/ch".equalsIgnoreCase(i.getDomain())).findFirst()
				.map(Identity::getDomainId).orElse(null);
	}
}
