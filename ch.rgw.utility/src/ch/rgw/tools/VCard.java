/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.rgw.tools;

import java.io.*;
import java.util.Hashtable;

/**
 * This class is a java representation of a vCard (http://www.imc.org/pdi/)
 * 
 */
public class VCard {
	Hashtable<String, String> elements = new Hashtable<String, String>();
	
	/**
	 * Construct a VCard from an InputStream. If the stream contains more than one vCard, only one
	 * will be loaded
	 * 
	 * @param in
	 *            the Stream containing the vCard-Data
	 * @throws VCardException
	 *             inf the stream does not contain a valid vCard
	 * @throws IOException
	 */
	public VCard(InputStream in) throws VCardException, IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		do {
			line = br.readLine();
		} while (!line.equalsIgnoreCase("begin:vcard"));
		line = br.readLine();
		while ((line != null) && (!line.equalsIgnoreCase("end:vcard"))) {
			String[] split = line.split("[:;]", 2);
			if (split.length > 1) {
				elements.put(split[0], split[1]);
			}
			line = br.readLine();
		}
	}
	
	/**
	 * find all elements in that vCard. An Element is one line, e.g.
	 * N;CHARSET=ISO-8859-1;ENCODING=QUOTED-PRINTABLE:Weirich;Gerry
	 * 
	 * @return a list of all elements
	 */
	public String[] getElements(){
		return elements.keySet().toArray(new String[0]);
	}
	
	/**
	 * find the named element
	 * 
	 * @param name
	 *            the name of the element to find
	 * @return the full element (with attributes and value)
	 */
	public String getElement(String name){
		return elements.get(name);
	}
	
	/**
	 * return all attributes of the given element
	 * 
	 * @param element
	 * @return
	 */
	public String[] getAttributes(String element){
		String[] s1 = element.split(":");
		String[] s2 = s1[0].split(";");
		return s2;
	}
	
	/**
	 * return the value of the given Element
	 * 
	 * @param element
	 * @return
	 */
	public String getValue(String element){
		String[] s1 = element.split(":");
		return (s1.length > 1) ? s1[1] : s1[0];
	}
	
	/**
	 * return the value of the given named element
	 * 
	 * @param name
	 *            name of the element
	 * @return value of the element or null
	 */
	public String getElementValue(String name){
		String element = getElement(name);
		if (element != null) {
			return getValue(element);
		}
		return null;
	}
	
	@SuppressWarnings("serial")
	public static class VCardException extends Exception {
		
	}
}
