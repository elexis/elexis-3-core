/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.text.model;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Verifier;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.rgw.tools.TimeTool;

/**
 * Ganz bescheiden: (S)tandard für den (A)ustausch (m)edizinischer (Da)ten in der (Schweiz) ->
 * SAmDaS. In Ermangelung eines besseren Standards sei dieser Name gestattet ;-) Samdas ist ein
 * XML-Schema, das die Übertragung medizinischer Krankengeschichten (Elecronic medical record, EMR)
 * zwischen verschiedenen Endanwendungen ermöglicht. Diese Klasse ist ein API dafür
 */
public class Samdas {
	public static final String ELEM_ROOT = "EMR"; //$NON-NLS-1$
	public static final String ELEM_TEXT = "text"; //$NON-NLS-1$
	public static final String ELEM_RECORD = "record"; //$NON-NLS-1$
	public static final Namespace ns = Namespace.getNamespace("samdas", "http://www.elexis.ch/XSD"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsxsi = Namespace.getNamespace(
		"xsi", "http://www.w3.org/2001/XML Schema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsschema = Namespace.getNamespace(
		"schemaLocation", "http://www.elexis.ch/XSD EMR.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
	
	private Document doc;
	private Element eRoot;
	
	/**
	 * Der String-Konstruktor erstellt ein Samdas aus der XML-Repräsentation. Wenn diese ungültig
	 * ist, wird ein Default-Dokument erstellt.
	 * 
	 * @param input
	 *            eine XML-Datei (oder irgendein Text, der dann komplett ins Text-Element des
	 *            Standard-Dokuments eingebunden wird)
	 */
	public Samdas(String input){
		SAXBuilder builder = new SAXBuilder();
		try {
			CharArrayReader car = new CharArrayReader(input.toCharArray());
			doc = builder.build(car);
			eRoot = doc.getRootElement();
			
		} catch (Exception e) {
			// SWTHelper.alert("Fehler beim Datenimport","Der XML-String enthält
			// formale Fehler oder kann nicht gelesen werden");
			// ExHandler.handle(e);
			doc = new Document();
			eRoot = new Element(ELEM_ROOT, ns);
			doc.setRootElement(eRoot);
			Element record = new Element(ELEM_RECORD, ns);
			Element text = new Element(ELEM_TEXT, ns);
			doc.getRootElement().addContent(record);
			record.addContent(text);
			text.setText(input);
		}
	}
	
	/**
	 * Der Default-Konstruktor erstellt ein leeres Standard-Dokument
	 * 
	 */
	public Samdas(){
		doc = new Document();
		eRoot = new Element(ELEM_ROOT, ns);
		// eRoot.addNamespaceDeclaration(nsxsi);
		// eRoot.addNamespaceDeclaration(nsschema);
		doc.setRootElement(eRoot);
	}
	
	public void setRoot(Element el){
		doc.removeContent();
		// doc.getRootElement().detach();
		eRoot = el;
		doc.setRootElement(el);
	}
	
	public Document getDocument(){
		return doc;
	}
	
	/**
	 * Get the contents of this Samdas in form of an XML-String
	 */
	@Override
	public String toString(){
		XMLOutputter xo = new XMLOutputter(Format.getRawFormat());
		return xo.outputString(doc);
	}
	
	/** Shortcut für Dokumente, die sowieso nur einen Record haben */
	public String getRecordText(){
		Element rec = getRecordElement();
		String ret = rec.getChildText(ELEM_TEXT, ns);
		return ret == null ? "" : ret; //$NON-NLS-1$
	}
	
	public Element getRecordElement(){
		Element ret = eRoot.getChild(ELEM_RECORD, ns);
		if (ret == null) {
			ret = new Element(ELEM_RECORD, ns);
			eRoot.addContent(ret);
		}
		return ret;
	}
	
	public Record getRecord(){
		return new Record(getRecordElement());
	}
	
	public void add(Record r){
		eRoot.addContent(r.eRecord);
	}
	
	/**
	 * A record is an Text entry. It can optionally contain style informations
	 * 
	 * @author Gerry
	 * 
	 */
	public static class Record {
		private static final String ELEM_SECTION = "section"; //$NON-NLS-1$
		public static final String ELEM_MARKUP = "markup"; //$NON-NLS-1$
		public static final String ELEM_XREF = "xref"; //$NON-NLS-1$
		public static final String ATTR_DATE = "date"; //$NON-NLS-1$
		public static final String ATTR_RESPONSIBLE_EAN = "responsibleEAN"; //$NON-NLS-1$
		public static final String ATTR_AUTHOR = "author"; //$NON-NLS-1$
		private Element eRecord;
		
		public Record(Element e){
			eRecord = e;
		}
		
		public String getAuthor(){
			return eRecord.getAttributeValue(ATTR_AUTHOR);
		}
		
		public String getResponsibleEAN(){
			return eRecord.getAttributeValue(ATTR_RESPONSIBLE_EAN);
		}
		
		public TimeTool getDate(){
			return new TimeTool(eRecord.getAttributeValue(ATTR_DATE));
		}
		
		public Element getTextElement(){
			Element ret = eRecord.getChild(ELEM_TEXT, ns);
			if (ret == null) {
				ret = new Element(ELEM_TEXT, ns);
				eRecord.addContent(ret);
			}
			return ret;
		}
		
		public void setText(String t){
			Element eText = getTextElement();
			eText.setText(getValidXMLString(t));
		}
		
		public String getText(){
			Element eText = getTextElement();
			return eText.getText();
		}
		
		@SuppressWarnings("unchecked")
		public List<XRef> getXrefs(){
			List<Element> lElm = eRecord.getChildren(ELEM_XREF, ns);
			List<XRef> ret = new ArrayList<XRef>(lElm.size());
			for (Element el : lElm) {
				ret.add(new XRef(el));
			}
			return ret;
		}
		
		@SuppressWarnings("unchecked")
		public List<Markup> getMarkups(){
			List<Element> lElm = eRecord.getChildren(ELEM_MARKUP, ns);
			List<Markup> ret = new ArrayList<Markup>(lElm.size());
			for (Element el : lElm) {
				ret.add(new Markup(el));
			}
			return ret;
		}
		
		@SuppressWarnings("unchecked")
		public List<Section> getSections(){
			List<Element> lElm = eRecord.getChildren(ELEM_SECTION, ns);
			List<Section> ret = new ArrayList<Section>(lElm.size());
			for (Element el : lElm) {
				ret.add(new Section(el));
			}
			return ret;
		}
		
		public void add(Range x){
			eRecord.addContent(x.el);
		}
		
		public void remove(Range x){
			eRecord.removeContent(x.el);
		}
		
		private String getValidXMLString(String source){
			StringBuilder ret = new StringBuilder();
			for (int i = 0, len = source.length(); i < len; i++) {
				// skip non valid XML characters
				if (Verifier.isXMLCharacter(source.charAt(i))) {
					ret.append(source.charAt(i));
				}
			}
			return ret.toString();
		}
	}
	
	/**
	 * A style is a display hint for a given Record or Range a Record can have several styles for
	 * several output media.
	 * 
	 */
	public static class Style {
		/** Opacity from 0 (transparent) to 1 (opaque) */
		private double opacity;
		/** Background color as rgb */
		private long rgbBackground;
	}
	
	/**
	 * A Range is a part of the text. It is defined by a position, a length and a type.
	 * 
	 */
	public static class Range {
		public static final String ATTR_LENGTH = "length"; //$NON-NLS-1$
		public static final String ATTR_FROM = "from"; //$NON-NLS-1$
		protected Element el;
		
		Range(Element e){
			el = e;
		}
		
		Range(String typ, int pos, int length){
			el = new Element(typ, ns);
			el.setAttribute(ATTR_FROM, Integer.toString(pos));
			el.setAttribute(ATTR_LENGTH, Integer.toString(length));
		}
		
		public int getPos(){
			return Integer.parseInt(el.getAttributeValue(ATTR_FROM));
		}
		
		public void setPos(int p){
			el.setAttribute(ATTR_FROM, Integer.toString(p));
		}
		
		public int getLength(){
			return Integer.parseInt(el.getAttributeValue(ATTR_LENGTH));
		}
		
		public void setLength(int length){
			el.setAttribute(ATTR_LENGTH, Integer.toString(length));
		}
	}
	
	/**
	 * An XRef is a range that defines a crossreference to some other piece of information it can
	 * define a class that can handle its contents
	 * 
	 * @author Gerry
	 * 
	 */
	public static class XRef extends Range {
		
		public static final String ATTR_ID = "id"; //$NON-NLS-1$
		public static final String ATTR_PROVIDER = "provider"; //$NON-NLS-1$
		
		XRef(Element e){
			super(e);
		}
		
		public XRef(String provider, String id, int pos, int length){
			super("xref", pos, length); //$NON-NLS-1$
			el.setAttribute(ATTR_PROVIDER, provider);
			el.setAttribute(ATTR_ID, id);
		}
		
		public String getProvider(){
			return el.getAttributeValue(ATTR_PROVIDER);
		}
		
		public String getID(){
			return el.getAttributeValue(ATTR_ID);
		}
	}
	
	/**
	 * A Markup is a Range that defines some text attributes
	 * 
	 * @author Gerry
	 * 
	 */
	public static class Markup extends Range {
		public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
		
		Markup(Element e){
			super(e);
		}
		
		public Markup(int pos, int length, String typ){
			super("markup", pos, length); //$NON-NLS-1$
			el.setAttribute(ATTR_TYPE, typ);
		}
		
		public String getType(){
			return el.getAttributeValue(ATTR_TYPE);
		}
	}
	
	/**
	 * A Section is a Markup that summarizes a piece of text unter a section header.
	 * 
	 * @author Gerry
	 * 
	 */
	public static class Section extends Range {
		private static final String ATTR_NAME = "name"; //$NON-NLS-1$
		
		Section(Element e){
			super(e);
		}
		
		public Section(int pos, int length, String name){
			super("section", pos, length); //$NON-NLS-1$
			el.setAttribute(ATTR_NAME, name);
		}
	}
	
	public static class Box {
		
	}
	
	@Deprecated
	public static class Finding {
		protected Element el;
		
		Finding(Element e){
			el = e;
		}
		
		Finding(String typ, String date, String labEAN, boolean abnormal){
			el = new Element(typ);
			el.setAttribute("date", date); //$NON-NLS-1$
			el.setAttribute("labEAN", labEAN); //$NON-NLS-1$
			el.setAttribute("abnormal", Boolean.toString(abnormal).toLowerCase()); //$NON-NLS-1$
		}
		
		public TimeTool getDate(){
			return new TimeTool(el.getAttributeValue("date")); //$NON-NLS-1$
		}
		
		public boolean isAbnormal(){
			return (el.getAttributeValue("abormal").equals("true")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	@Deprecated
	public static class Analyse extends Finding {
		public Analyse(){
			super(new Element("analysis")); //$NON-NLS-1$
		}
	}
	
	@Deprecated
	public static class Image extends Finding {
		public Image(){
			super(new Element("image")); //$NON-NLS-1$
		}
	}
	
	@Deprecated
	public static class ECG extends Finding {
		public ECG(){
			super(new Element("ecg")); //$NON-NLS-1$
		}
	}
	
}
