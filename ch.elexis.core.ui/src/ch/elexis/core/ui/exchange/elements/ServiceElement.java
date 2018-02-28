/*******************************************************************************
 * Copyright (c) 2010 by G. Weirich
 * This program is based on the Sgam-Exchange project,
 * (c) SGAM-Informatics
 * All rights resevred
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.exchange.elements;

import java.util.List;
import java.util.Optional;

import org.jdom.Element;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.ui.exchange.XChangeContainer;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.dto.CodeElementDTO;
import ch.rgw.tools.TimeTool;

public class ServiceElement extends XChangeElement {
	
	private static final long serialVersionUID = 6382517263003793221L;
	public static final String XMLNAME = "service";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_CONTRACT_NAME = "contractName";
	public static final String ATTR_CONTRACT_CODE = "contractCode";
	public static final String ATTR_MINUTES = "minutes";
	public static final String ATTR_COST = "cost";
	public static final String ATTR_PRICE = "price";
	public static final String ELEMENT_XID = XidElement.XMLNAME;
	
	public ServiceElement asExporter(XChangeExporter p, IVerrechenbar iv){
		asExporter(p);
		setAttribute(ATTR_NAME, iv.getText());
		setAttribute(ATTR_CONTRACT_CODE, iv.getCode());
		setAttribute(ATTR_CONTRACT_NAME, iv.getCodeSystemName());
		setAttribute(ATTR_MINUTES, Integer.toString(iv.getMinutes()));
		setAttribute(ATTR_COST, iv.getKosten(new TimeTool()).getCentsAsString());
		setAttribute(ATTR_PRICE, Integer.toString(iv.getTP(new TimeTool(), (IFall) null)));
		add(new XidElement().asExporter(p, iv));
		return this;
	}
	
	public ServiceElement asExporter(XChangeExporter sender, CodeElementDTO ic){
		asExporter(sender);
		setAttribute(ATTR_NAME, ic.getText());
		setAttribute(ATTR_CONTRACT_CODE, ic.getCode());
		setAttribute(ATTR_CONTRACT_NAME, ic.getCodeSystemName());
		// optional fields for IVerrechenbar
		ICodeElementService service = CodeElementServiceHolder.getService();
		if (service != null) {
			Optional<ICodeElement> element =
				service.createFromString(ic.getCodeSystemName(), ic.getCode(),
					CodeElementServiceHolder.emtpyContext());
			if (element.isPresent() && element.get() instanceof IVerrechenbar) {
				IVerrechenbar iv = (IVerrechenbar) element.get();
				setAttribute(ATTR_MINUTES, Integer.toString(iv.getMinutes()));
				setAttribute(ATTR_COST, iv.getKosten(new TimeTool()).getCentsAsString());
				setAttribute(ATTR_PRICE, Integer.toString(iv.getTP(new TimeTool(), (IFall) null)));
				add(new XidElement().asExporter(sender, iv));
			}
		}
		return this;
	}
	
	public IVerrechenbar createObject(XChangeContainer home, Element el){
		XidElement xide = (XidElement) getChild(XidElement.XMLNAME, XidElement.class);
		List<IPersistentObject> objs = xide.findObject();
		for (IPersistentObject po : objs) {
			if (po instanceof IVerrechenbar) {
				return (IVerrechenbar) po;
			}
		}
		Eigenleistung egl =
			new Eigenleistung(el.getAttributeValue(ATTR_CONTRACT_CODE),
				el.getAttributeValue(ATTR_NAME), el.getAttributeValue(ATTR_COST),
				el.getAttributeValue(ATTR_PRICE));
		return egl;
	}
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
}
