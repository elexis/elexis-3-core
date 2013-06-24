/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.data.interfaces;

import java.util.List;

import ch.elexis.core.data.PersistentObject;
import ch.rgw.tools.Result;

/**
 * Interface that defines a number of data types to expose and grant access to them
 * 
 * @author gerry
 * 
 */
public interface IDataAccess {
	public enum TYPE {
		STRING, INTEGER, DOUBLE
	}
	
	public static final int INVALID_PARAMETERS = 1;
	public static final int OBJECT_NOT_FOUND = 2;
	
	public static class Element {
		private final TYPE typ;
		private final String name;
		private final String placeholder;
		private final Class<? extends PersistentObject> reference;
		private final int numOfParams;
		
		public Element(final TYPE typ, final String name, final String ph,
			final Class<? extends PersistentObject> ref, final int numOfParams){
			this.typ = typ;
			this.name = name;
			this.placeholder = ph;
			this.reference = ref;
			this.numOfParams = numOfParams;
		}
		
		public Element(TYPE typ, String name, Class<? extends PersistentObject> ref, int numOfParams){
			this(typ, name, "-", ref, numOfParams);
		}
		
		public TYPE getTyp(){
			return typ;
		}
		
		public String getName(){
			return name;
		}
		
		public String getPlaceholder(){
			return placeholder;
		}
	}
	
	/**
	 * Name of the data accessor to display
	 */
	public String getName();
	
	/**
	 * Short description to the data accessor
	 */
	public String getDescription();
	
	/**
	 * return a list of all data provided by this interface
	 * 
	 * @return a (possibly empty) List of Elements
	 */
	public List<Element> getList();
	
	/**
	 * return specified data
	 * 
	 * @param descriptor
	 *            description of the Object to retrieve (name and fields)
	 * @param dependentObject
	 *            Type of PersistentObject this data depends on
	 * @param dates
	 *            date definition. either a date string or "all" or "last"
	 * @param params
	 *            parameters that might be required for this element
	 * @return some object depending of the request
	 */
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject,
		String dates, String[] params);
	
}
