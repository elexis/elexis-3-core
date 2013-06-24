/*******************************************************************************
 * Copyright (c) 2007-2009, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    G. Weirich - additional methods
 *    medshare GmbH - XML validator
 *    
 *******************************************************************************/

package ch.rgw.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ch.rgw.crypt.Base64Coder;

/**
 * This class provides various helper methods for handling XML data.
 * 
 * @author danlutz
 */

public class XMLTool {
	static final String JAXP_SCHEMA_LANGUAGE =
		"http://java.sun.com/xml/jaxp/properties/schemaLanguage"; //$NON-NLS-1$
	
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource"; //$NON-NLS-1$
	
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	
	public static List<String> validateSchema(String xmlDocumentUrl){
		return validateSchema(null, xmlDocumentUrl);
	}
	
	public static List<String> validateSchema(String schemaUrl, Source source){
		MyErrorHandler errorHandler = new MyErrorHandler();
		try {
			// 1. Lookup a factory for the W3C XML Schema language
			SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA);
			
			// 2. Compile the schema.
			// Here the schema is loaded from a java.io.File, but you could use
			// a java.net.URL or a javax.xml.transform.Source instead.
			Schema schema = factory.newSchema();
			if (schemaUrl != null) {
				File schemaLocation = new File(schemaUrl);
				schema = factory.newSchema(schemaLocation);
			}
			
			// 3. Get a validator from the schema.
			Validator validator = schema.newValidator();
			
			// 5. Check the document
			validator.setErrorHandler(errorHandler);
			validator.validate(source);
		} catch (Exception ex) {
			errorHandler.exception(ex);
		}
		return errorHandler.getMessageList();
	}
	
	public static List<String> validateSchema(String schemaUrl, String xmlDocumentUrl){
		Source source = new StreamSource(xmlDocumentUrl);
		return validateSchema(schemaUrl, source);
	}
	
	private static class MyErrorHandler implements ErrorHandler {
		public List<Exception> exceptions = new Vector<Exception>();
		
		public void error(SAXParseException exception) throws SAXException{
			exceptions.add(exception);
		}
		
		public void fatalError(SAXParseException exception) throws SAXException{
			exceptions.add(exception);
		}
		
		public void warning(SAXParseException exception) throws SAXException{
			// Nothing
		}
		
		public void exception(Exception exception){
			exceptions.add(exception);
		}
		
		public List<String> getMessageList(){
			List<String> messageList = new Vector<String>();
			for (Exception ex : exceptions) {
				String msg = ex.getMessage();
				if (msg == null || msg.length() == 0) {
					msg = ex.toString();
				}
				messageList.add(msg);
			}
			return messageList;
		}
	}
	
	public static String moneyToXmlDouble(Money money){
		int cents = money.getCents();
		
		// we force to use a literal "."
		
		// honor signum
		int absCents = Math.abs(cents);
		int signum = Integer.signum(cents);
		int abs = absCents / 100;
		int frac = absCents % 100;
		
		String xmlDouble = String.format("%d.%02d", signum * abs, frac);
		return xmlDouble;
		
	}
	
	public static Money xmlDoubleToMoney(String xmlDouble) throws NumberFormatException{
		if (xmlDouble == null) {
			throw new NumberFormatException("xmlDouble must not be null");
		}
		Double d = Double.parseDouble(xmlDouble);
		return new Money(d);
	}
	
	/**
	 * Convert a double value to String conforming to the double datatype of the XML specification.
	 * (This means mainly, that we have to use the swiss "." also if the PC's locale is set to
	 * germany or austria.)
	 * 
	 * @param value
	 *            the value to be converted
	 * @param factionalDigits
	 *            the number of digits after the point
	 * @return the formated String
	 */
	public static String doubleToXmlDouble(double value, int factionalDigits){
		long cents = Math.round(value * 100);
		// we force to use a literal "."
		/*
		 * String xmlDouble = String.format("%d.%02d", cents / 100, cents % 100);
		 * 
		 * return xmlDouble;
		 */
		// honor signum
		int absCents = Math.abs((int) cents);
		int signum = Integer.signum((int) cents);
		int abs = absCents / 100;
		int frac = absCents % 100;
		String dec = "%d.%0" + Integer.toString(factionalDigits) + "d";
		// The - sign is lost if the integer is 0
		if ((abs == 0) && (signum < 0)) {
			dec = "-" + dec;
		}
		
		String xmlDouble = String.format(dec, signum * abs, frac);
		return xmlDouble;
		
	}
	
	/**
	 * Convert a XML-Table formatted like &lt;table&gt; &lt;row&gt; &lt;col1&gt;Col 1&lt;col1/&gt;
	 * &lt;col2&gt;Col 2&lt;col2/&gt; &lt;/row&gt; &lt;row&gt; ... &lt;/row&gt; &lt;/table&gt; to a
	 * csv table
	 * 
	 * @param table
	 *            the table to convert
	 * @param separator
	 *            String that separates columns
	 * @return a string containing the csv table. Rows separated by \n, colums separated by
	 *         separator
	 */
	@SuppressWarnings("unchecked")
	public static String XMLTableToCSVTable(Element table, String separator){
		List<Element> rows = table.getChildren();
		StringBuilder ret = new StringBuilder();
		for (Element row : rows) {
			List<Element> cols = row.getChildren();
			for (Element col : cols) {
				ret.append(col.getText()).append(separator);
			}
			ret.replace(ret.length() - separator.length(), ret.length(), "\n");
		}
		return ret.toString();
	}
	
	/**
	 * Convert a XML-Table formatted like &lt;table&gt; &lt;row&gt; &lt;col1&gt;Col 1&lt;col1/&gt;
	 * &lt;col2&gt;Col 2&lt;col2/&gt; &lt;/row&gt; &lt;row&gt; ... &lt;/row&gt; &lt;/table&gt; to a
	 * html table
	 * 
	 * @param table
	 *            the table to convert
	 * @return a string containing the html table.
	 */
	@SuppressWarnings("unchecked")
	public static String XMLTableToHTMLTable(Element table){
		List<Element> rows = table.getChildren();
		StringBuilder ret = new StringBuilder();
		ret.append("<table>");
		for (Element row : rows) {
			ret.append("<tr>");
			List<Element> cols = row.getChildren();
			for (Element col : cols) {
				ret.append("<td>").append(col.getText()).append("</td>");
			}
			ret.append("</tr>");
		}
		ret.append("</table>");
		return ret.toString();
	}
	
	/**
	 * Conversion betweeen Elexis id's and XML ID types. XML id types must not begin with a number
	 * but may contain letters and numbers. Elexis ID's are always hexadecimal strings thus will
	 * never contain a letter other than a-f but might start with a number. Thus if it starts with a
	 * number, we prefix an "x"
	 * 
	 * @param id
	 *            an elexis id
	 * @return a String conforming to tghe XML ID type
	 */
	public static String idToXMLID(String id){
		if (id != null) {
			if (id.matches("[0-9].+")) {
				return "x" + id;
			}
		}
		return id;
	}
	
	/**
	 * Since elexis id's never contain the letter "x" we can be sure that a starting letter x can be
	 * removed to leave us with the original elexis id
	 * 
	 * @param xmlid
	 *            an XML ID
	 * @return the conforming elexis id
	 */
	public static String xmlIDtoID(String xmlid){
		if (xmlid != null) {
			if (xmlid.startsWith("x")) {
				return xmlid.substring(1);
			}
		}
		return xmlid;
	}
	
	/**
	 * Convert a TimeTool into an XML dateTime type
	 * 
	 * @param dateTime
	 * @return
	 */
	public static String dateTimeToXmlDateTime(String dateTime){
		TimeTool tt = new TimeTool(dateTime);
		return tt.toString(TimeTool.DATETIME_XML);
	}
	
	/**
	 * Copnvert a date part of a TimeTool to an XML date type
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToXmlDate(String date){
		return new TimeTool(date).toString(TimeTool.DATE_ISO);
	}
	
	/**
	 * Convert a HashMap of String/Object pairs into a SOAP compatible XML structure. Ad this time,
	 * only String, int, long, byte, byte[] and Hashmaps thereof are supported as Object types
	 * 
	 * @param hash
	 * @param name
	 * @param ns
	 * @return
	 */
	public static Element HashMapToXML(HashMap<String, Object> hash, String name, Namespace ns){
		Element ret = new Element("hash", ns);
		ret.setAttribute("name", name);
		Set<Entry<String, Object>> vars = hash.entrySet();
		for (Entry<String, Object> entry : vars) {
			Element var;
			String n = entry.getKey();
			Object o = entry.getValue();
			if (o instanceof String) {
				var = new Element("string", ns);
				var.setAttribute("name", n);
				var.setText((String) o);
			} else if ((o instanceof Integer) || (o instanceof Long) || (o instanceof Short)
				|| (o instanceof Byte)) {
				var = new Element("int", ns);
				var.setAttribute("name", "n");
				var.setText(o.toString());
			} else if (o instanceof HashMap) {
				var = HashMapToXML((HashMap) o, n, ns);
			} else if (o instanceof byte[]) {
				var = new Element("array", ns);
				var.setAttribute("name", n);
				var.setText(new String(Base64Coder.encode((byte[]) o)));
			} else {
				var = null;
			}
			if (var == null) {
				return null;
			}
			ret.addContent(var);
		}
		return ret;
	}
	
	public static HashMap<String, Object> XMLToHashMap(Element elem){
		HashMap<String, Object> ret = new HashMap<String, Object>();
		List<Element> vars = elem.getChildren();
		for (Element var : vars) {
			String type = var.getName();
			
		}
		return null;
	}
	
	public static boolean writeXMLDocument(Document doc, String dest){
		try {
			FileOutputStream fout = new FileOutputStream(dest);
			OutputStreamWriter cout = new OutputStreamWriter(fout, "UTF-8");
			XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
			xout.output(doc, cout);
			cout.close();
			fout.close();
			return true;
		} catch (Exception e) {
			ExHandler.handle(e);
			return false;
		}
		
	}
	
}
