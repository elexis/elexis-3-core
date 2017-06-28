/*******************************************************************************
 * Copyright (c) 2008-2009 by G. Weirich
 * This program is based on the Sgam-Exchange project,
 * (c) SGAM-Informatics
 * All rights reserved
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.exchange.elements;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.ui.exchange.XChangeContainer;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.core.ui.exchange.XChangeImporter;
import ch.elexis.data.Xid;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.XMLTool;

/**
 * Base class for all xChange Elements
 * 
 * @author gerry
 * 
 */
public abstract class XChangeElement {
	public static final String ATTR_ID = "id";
	public static final String ATTR_DATE = "date";
	protected XChangeExporter sender;
	private XChangeImporter reader;
	protected Element ex;
	
	public XChangeElement(){
		ex = new Element(getXMLName(), XChangeContainer.ns);
	}
	
	public XChangeElement asImporter(XChangeImporter reader, Element el){
		this.reader = reader;
		ex = el == null ? new Element(getXMLName(), XChangeContainer.ns) : el;
		return this;
	}
	
	public XChangeElement asExporter(XChangeExporter sender){
		this.sender = sender;
		if (ex == null) {
			ex = new Element(getXMLName(), XChangeContainer.ns);
		}
		return this;
	}
	
	/**
	 * Format for text representation of the element contents
	 * 
	 * @author gerry
	 * 
	 */
	public enum FORMAT {
		PLAIN, XML, HTML
	};
	
	public static final int OK = 0;
	public static final int FORMAT_NOT_SUPPORTED = 1;
	
	public Element getElement(){
		return ex;
	}
	
	public void setElement(Element e){
		ex = e;
	}
	
	public void setReader(XChangeImporter reader){
		sender = null;
		this.reader = reader;
	}
	
	public void setWriter(XChangeExporter writer){
		reader = null;
		sender = writer;
	}
	
	public XChangeContainer getContainer(){
		if (sender == null) {
			if (reader == null) {
				return null;
			} else {
				return reader.getContainer();
			}
		} else {
			return sender.getContainer();
		}
	}
	
	public XChangeImporter getReader(){
		return reader;
	}
	
	public XChangeExporter getSender(){
		return sender;
	}
	
	/*
	 * public void setContainer(XChangeContainer c){ parent = c; }
	 */
	public abstract String getXMLName();
	
	/**
	 * return an attribute value of the underlying element.
	 * 
	 * @param name
	 *            name of the atribute
	 * @return the value which can be an empty String but is never null.
	 */
	public String getAttr(final String name){
		String ret = ex.getAttributeValue(name);
		return ret == null ? "" : ret;
	}
	
	/**
	 * append a XID that consists solely of the local identity id
	 * 
	 * @param id
	 */
	public void setDefaultXid(String id){
		XidElement xid = new XidElement();
		xid.addIdentity(XidConstants.DOMAIN_ELEXIS, id, Xid.ASSIGNMENT_LOCAL, true);
		xid.setMainID(null);
		add(xid);
	}
	
	public String getID(){
		String rawID = getAttr(ATTR_ID);
		if (rawID.length() == 0) {
			XidElement eXid = getXid();
			if (eXid != null) {
				rawID = eXid.getAttr(ATTR_ID);
			}
		}
		// return XMLTool.xmlIDtoID(rawID);
		return rawID;
	}
	
	public void add(final XChangeElement el){
		ex.addContent(el.ex);
	}
	
	public XidElement getXid(){
		XidElement xid = new XidElement();
		Element el = ex.getChild(XidElement.XMLNAME, XChangeContainer.ns);
		if (el == null) {
			return null;
		}
		xid.setElement(el);
		return xid;
	}
	
	/**
	 * FInd all children of a specified subclass of XChangeElement with a specified element name
	 * 
	 * @param name
	 * @param clazz
	 * @return a possibly empty list or null on errors
	 */
	public List<? extends XChangeElement> getChildren(final String name,
		final Class<? extends XChangeElement> clazz){
		LinkedList<XChangeElement> ret = new LinkedList<XChangeElement>();
		for (Object el : ex.getChildren(name, XChangeContainer.ns)) {
			try {
				XChangeElement xc = clazz.getConstructor().newInstance();
				xc.setElement((Element) el);
				ret.add(xc);
			} catch (Exception e) {
				ExHandler.handle(e);
				return null;
			}
		}
		return ret;
	}
	
	public XChangeElement getChild(String name, Class<? extends XChangeElement> clazz){
		Element el = ex.getChild(name, XChangeContainer.ns);
		if (el == null) {
			return null;
		}
		XChangeElement ret;
		try {
			ret = clazz.getConstructor().newInstance();
			ret.setWriter(getSender());
			ret.setReader(getReader());
			ret.setElement(el);
			return ret;
		} catch (Exception e) {
			ExHandler.handle(e);
			return null;
		}
	}
	
	/**
	 * create a string representation of this Element. Subclasses should override
	 * 
	 * @param format
	 *            one of gthe FORMAT constants
	 * @return a String representation if the format was supported. The default implementation
	 *         returns always "Format not supported"
	 */
	public Result<String> toString(final FORMAT format){
		return new Result<String>(Result.SEVERITY.ERROR, FORMAT_NOT_SUPPORTED,
			"Format not supported", null, true);
	}
	
	public void setAttribute(String attr, String value){
		ex.setAttribute(attr, XMLTool.getValidXMLString(value));
	}
}
