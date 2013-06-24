/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.IllegalAddException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ch.rgw.tools.Log;

/**
 * This class helps exporting/importing of database data.<br>
 * Export is transforming database values into an xml sheet. <br>
 * Import is reading an xml sheet and updating the database.<br>
 * XML structure looks like: <br>
 * <ARTIKEL javaclass="ch.elexis.data.Artikel"> <br>
 * <UID field="ID">r895a3a395be62a6e19c1103</UID> <br>
 * <EAN>7680573770024</EAN> <br>
 * ... other field values <br>
 * </ARTIKEL> <br>
 * 
 */
public class XML2Database {
	// TODO -> to delete?
	protected static Log log = Log.get(XML2Database.class.getName());
	
	private final static String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	private final static String LIST_TAG = "DATALIST";
	private final static String UID_TAG = "UID";
	private final static String UID_VERSION_ATTRIBUTE = "version";
	private final static String CLASS_ATTRIBUTE = "javaclass";
	private final static String TEXT_TAG = "#text"; // Tags to ignore
	
	@SuppressWarnings("unchecked")
	private HashMap<Class, HashMap<String, List<String>>> cache =
		new HashMap<Class, HashMap<String, List<String>>>();
	
	private static class DataField {
		final String name;
		final String value;
		
		public DataField(String name, String value){
			super();
			this.name = name;
			this.value = value;
		}
	}
	
	private static class XMLObject {
		private PersistentObject object = null;
		private StringBuffer buffer = new StringBuffer();
		private final Stack<String> stack = new Stack<String>();
		private boolean elementOpened = false;
		
		private XMLObject(final PersistentObject object){
			this.object = object;
		}
		
		private void openElement(final String name){
			if (elementOpened) {
				writeCloseSign(true);
			}
			addTab();
			buffer.append("<" + name);
			elementOpened = true;
			stack.push(name);
		}
		
		private void writeCloseSign(boolean newLine){
			buffer.append(">");
			if (newLine) {
				buffer.append("\n");
			}
			elementOpened = false;
		}
		
		private void addTab(){
			for (int i = 0; i < stack.size(); i++) {
				buffer.append("\t");
			}
		}
		
		private void closeElement(){
			if (elementOpened) {
				writeCloseSign(true);
				stack.pop();
			} else {
				if (!stack.isEmpty()) {
					String name = stack.pop();
					buffer.append("</" + name + ">\n");
				}
			}
		}
		
		private void addAttribute(final String attribute, final String value){
			if (elementOpened) {
				buffer.append(" " + attribute + "=\"" + value + "\"");
			} else {
				throw new IllegalAddException("");
			}
		}
		
		private void addValue(final String value){
			if (elementOpened) {
				writeCloseSign(false);
			}
			buffer.append(value);
		}
		
		/**
		 * Exports an array
		 * 
		 * @return
		 */
		private String exportData(){
			String[] fieldList = object.getExportFields();
			String[] resultList = new String[fieldList.length];
			object.get(fieldList, resultList);
			
			openElement(object.getTableName());
			addAttribute(CLASS_ATTRIBUTE, object.getClass().getName());
			
			// Primary key
			openElement(UID_TAG);
			addAttribute(UID_VERSION_ATTRIBUTE, object.getExportUIDVersion());
			addValue(object.getExportUIDValue());
			closeElement();
			
			// Fields
			for (int i = 0; i < fieldList.length; i++) {
				String name = fieldList[i];
				String value = resultList[i];
				openElement(name);
				addValue(value);
				closeElement();
			}
			
			closeElement();
			
			return buffer.toString();
		}
	}
	
	/********************************************************************************
	 * PRIVATE METHODS
	 */
	
	@SuppressWarnings("unchecked")
	private List<String> getValues(Class javaClass, String uidValue){
		HashMap<String, List<String>> map = cache.get(javaClass);
		if (map == null) {
			map = new HashMap<String, List<String>>();
			cache.put(javaClass, map);
			
			Query<PersistentObject> query = new Query<PersistentObject>(javaClass);
			List<PersistentObject> poList = query.execute();
			for (PersistentObject po : poList) {
				String value = po.getExportUIDValue();
				List<String> idList = map.get(value);
				if (idList == null) {
					idList = new Vector<String>();
					map.put(value, idList);
				}
				idList.add(po.getId());
			}
		}
		return map.get(uidValue);
	}
	
	/**
	 * Import
	 * 
	 * @param tableNode
	 * @param overwrite
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void importData(final Node tableNode, final boolean overwrite){
		try {
			String className =
				tableNode.getAttributes().getNamedItem(CLASS_ATTRIBUTE).getNodeValue();
			Class javaClass = Class.forName(className);
			String uidValue = null;
			String uidVersion = null;
			
			// Read fields
			List<DataField> fieldList = new Vector<DataField>();
			NodeList nodeList = tableNode.getChildNodes();
			for (int index = 0; index < nodeList.getLength(); index++) {
				Node fieldNode = nodeList.item(index);
				if (!TEXT_TAG.equals(fieldNode.getNodeName())) {
					if (UID_TAG.equals(fieldNode.getNodeName())) {
						uidValue = fieldNode.getTextContent();
						if (fieldNode.getAttributes().getNamedItem(UID_VERSION_ATTRIBUTE) == null) {
							throw new IllegalArgumentException("No UID export version found!");
						}
						uidVersion =
							fieldNode.getAttributes().getNamedItem(UID_VERSION_ATTRIBUTE)
								.getNodeValue();
					} else {
						fieldList.add(new DataField(fieldNode.getNodeName(), fieldNode
							.getTextContent()));
					}
				}
			}
			
			// Create PersistentObject
			String[] fields = new String[fieldList.size()];
			String[] results = new String[fieldList.size()];
			
			for (int i = 0; i < fieldList.size(); i++) {
				fields[i] = fieldList.get(i).name;
				results[i] = fieldList.get(i).value;
			}
			
			// Check uidField
			Constructor<PersistentObject> constructor = javaClass.getDeclaredConstructor();
			PersistentObject po = constructor.newInstance();
			if (!uidVersion.equals(po.getExportUIDVersion())) {
				throw new IllegalArgumentException("UID export versions are different!");
			}
			
			// Read values
			List<String> list = getValues(javaClass, uidValue);
			
			if (list == null || list.size() == 0) {
				// Create new
				po.create(null);
				po.set(fields, results);
			} else if (overwrite) {
				for (String idValue : list) {
					Constructor<PersistentObject> tmpConstr =
						javaClass.getDeclaredConstructor(String.class);
					PersistentObject tmpPo = tmpConstr.newInstance(idValue);
					System.out.println("Name: " + tmpPo.get("Name"));
					tmpPo.set(fields, results);
				}
			}
		} catch (Exception e) {
			log.log(e.getMessage(), Log.ERRORS);
		}
	}
	
	/**
	 * Parsing XML Document with SAX
	 */
	private Document readXmlDocument(InputSource is, String docDescription) throws SAXException,
		ParserConfigurationException, java.io.IOException{
		if (is == null) {
			return null;
		}
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(is);
	}
	
	/**
	 * Imports data into database. <br>
	 * XML could be a list of data objects or only one data object
	 */
	private void importXML(final String xml, final boolean overwrite){
		try {
			Document document = readXmlDocument(new InputSource(new StringReader(xml)), "Import");
			NodeList rootNodeList = document.getChildNodes();
			for (int rootIndex = 0; rootIndex < rootNodeList.getLength(); rootIndex++) {
				Node rootNode = rootNodeList.item(rootIndex);
				if (LIST_TAG.equals(rootNode.getNodeName())) {
					NodeList nodeList = rootNode.getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node tableNode = nodeList.item(i);
						if (!TEXT_TAG.equals(tableNode.getNodeName())
							&& tableNode.getAttributes() != null) {
							importData(tableNode, overwrite);
						}
					}
				} else {
					importData(rootNode, overwrite);
				}
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/********************************************************************************
	 * PUBLIC METHODS
	 */
	
	/**
	 * Imports data into database. <br>
	 * XML could be a list of data objects or only one data object
	 */
	public static void importData(final String data, final boolean overwrite){
		new XML2Database().importXML(data, overwrite);
	}
	
	/**
	 * Exports a persistent object
	 * 
	 * @return
	 */
	public static String exportData(final PersistentObject object){
		StringBuffer buffer = new StringBuffer(HEADER);
		buffer.append(new XMLObject(object).exportData());
		return buffer.toString();
	}
	
	/**
	 * Exports an array
	 * 
	 * @return
	 */
	public static String exportData(final List<? extends PersistentObject> dataList){
		StringBuffer buffer = new StringBuffer(HEADER);
		buffer.append("<" + LIST_TAG + ">\n");
		for (PersistentObject object : dataList) {
			buffer.append(new XMLObject(object).exportData());
		}
		buffer.append("</" + LIST_TAG + ">\n");
		return buffer.toString();
	}
}
