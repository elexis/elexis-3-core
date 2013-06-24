/*******************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.datatypes;

import java.util.Map;

/**
 * A PersistentObjectFactory can create Instances of any class that is in the classpath and
 * implements IPersistentObject
 * 
 * @author gerry
 * 
 */
public interface IPersistentObjectManager {
	
	/**
	 * Fetch an IPersistentObject from the storage. This is a replacement for the static
	 * load(String)-Methods of earlier PersistentObject implementations
	 * 
	 * @param clazz
	 *            full qualified class name of the Object to load
	 * @param id
	 *            GUID of the object to load
	 * @return the newly created Object, populated with according properties from the data storage,
	 *         or null if an Object with the given id does not exist.
	 * @throws ElexisCoreException
	 *             if access to the Object was not possible (e.g. due to missing class or missing
	 *             access to the storage)
	 */
	public IPersistentObject fetch(Class<?> typ, String id);
	
	/**
	 * Create an IPersistentObject.
	 * 
	 * @param clazz
	 *            full qualified class name of the Object to create
	 * @param id
	 *            id of the Object to create. Can be null, then the id will be created by the
	 *            framework
	 * @param persistent
	 *            true if the Object should be persisted in the storage
	 * @return the newly created Object
	 * @throws ElexisCoreException
	 *             if Creation wos not possible
	 */
	public IPersistentObject create(Class<?> typ, String id, boolean persistent);
	
	/**
	 * set an arbitrary property to an IPersistentObject
	 * 
	 * @param object
	 *            IPersistentObject to modify
	 * @param propertyName
	 *            Name of the property to change
	 * @param propertyValue
	 *            new value for the property
	 * @throws ElexisStorageException
	 *             if the Storage is not ready
	 * @throws ElexisCoreException
	 *             if something is not correct
	 */
	// public void setProperty(IPersistentObject object, String propertyName, Object propertyValue)
	// throws ElexisStorageException, ElexisCoreException;
	
	/**
	 * get an arbitrary Property from an IPersistentObject
	 * 
	 * @param object
	 *            IPersistentObject to query
	 * @param propertyName
	 *            name of the Property to read
	 * @return the Property or null if no such property exists.
	 * @throws ElexisStorageException
	 *             id the storage is not ready
	 */
	// public Object getProperty(IPersistentObject object, String propertyName) throws
	// ElexisStorageException;
	
	public void setBinding(Map<String, Class<? extends IPersistentObject>> map);
	
	public IPersistentObject[] executeQuery(Query qbe);
}
