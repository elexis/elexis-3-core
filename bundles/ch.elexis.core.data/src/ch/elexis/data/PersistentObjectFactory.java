/*******************************************************************************
 * Copyright (c) 2005-2014, G. Weirich, Elexis and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT - <office@medevit.at>
 *******************************************************************************/
package ch.elexis.data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.jdt.Nullable;
import ch.rgw.tools.ExHandler;

public class PersistentObjectFactory implements IExecutableExtension {
	private static Logger log = LoggerFactory.getLogger(PersistentObjectFactory.class);
	
	private static HashMap<String, PersistentObjectFactory> poFactoryCache = new HashMap<>();
	
	private static final String CLASS = "Class";
	
	/**
	 * Create a template of the provided class type, without creating a corresponding database
	 * entry. If the core is not able to create the resp. class, all plug-ins contributing a
	 * PersistentObjectFactory will be queried.
	 * 
	 * @param type
	 *            the requested class type
	 * @return a non-persisted object of class type or <code>null</code>
	 * @throws PersistenceException
	 */
	@SuppressWarnings("unchecked")
	public PersistentObject createTemplate(Class type){
		// try to resolve factory from cache
		PersistentObjectFactory persistentObjectFactory = poFactoryCache.get(type.getName());
		if (persistentObjectFactory != null) {
			PersistentObject poTemplate = persistentObjectFactory.doCreateTemplate(type);
			if (poTemplate != null) {
				return poTemplate;
			}
			log.info("Could not create template for [" + type.getName() + "] with cached factory ["
				+ persistentObjectFactory + "]");
		}
		
		try {
			PersistentObject po = (PersistentObject) type.newInstance();
			return po;
		} catch (IllegalAccessException ex) {
			List<PersistentObjectFactory> contributedFactories =
				Extensions.getClasses(ExtensionPointConstantsData.PERSISTENT_REFERENCE, CLASS);
			for (PersistentObjectFactory po : contributedFactories) {
				PersistentObject ret = po.doCreateTemplate(type);
				if (ret != null) {
					// found a responsible factory, cache it
					poFactoryCache.put(type.getName(), po);
					return ret;
				}
			}
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"create: Couldn't create object " + ex.getMessage(), ex,
					ElexisStatus.LOG_ERRORS);
			throw new PersistenceException(status);
		}
		return null;
	}
	
	protected PersistentObject doCreateTemplate(Class<? extends PersistentObject> typ){
		try {
			return (PersistentObject) typ.newInstance();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Helper-Funktion, die Objekte eines beliebigen abgeleiteten Typs mit beliebigen Feldvorgaben
	 * erstellen kann.
	 * 
	 * @param typ
	 *            Die Klasse des zu erstellenden Objekts
	 * @param fields
	 *            Die initial zu belegenden Felder. ID darf nicht angegeben werden.
	 * @param values
	 *            Die Werte für die Felder
	 * @return Das Objekt bei Erfolg, sonst null
	 */
	public PersistentObject create(Class<? extends PersistentObject> typ, String[] fields,
		String[] values){
		PersistentObject template = createTemplate(typ);
		template.create(null);
		if ((template != null) && (template.set(fields, values) == true)) {
			return template;
		}
		return null;
	}
	
	/**
	 * Create an object of a derived class given a pseudo de-serialization code, e.g.
	 * <code>ch.elexis.artikel_ch.data.Medikament::ca8bb5c27bdd67d5f011821</code>. If the object can
	 * not be created by the core, all plug-ins contributing a {@link #PersistentObjectFactory()}
	 * are queried.
	 * 
	 * @param code
	 *            the storeToString as shown in the above example
	 * @return the de-serialized object, or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public @Nullable PersistentObject createFromString(final String code){
		PersistentObject po = createFromString(code, null);
		if (po == null) {
			log.warn("Could not createFromString [{}]", code);
		}
		return po;
	}
	
	/**
	 * Create an object of a derived class given a pseudo de-serialization code, e.g.
	 * <code>ch.elexis.artikel_ch.data.Medikament::ca8bb5c27bdd67d5f011821</code>. If the object can
	 * not be created by the core, all plug-ins contributing a {@link #PersistentObjectFactory()}
	 * are queried.
	 * 
	 * @param code
	 *            the storeToString as shown in the above example
	 * @param dbConnection
	 *            the db connection used by the created PersistenObject, if not defined default is
	 *            used
	 * @return the de-serialized object, or <code>null</code>
	 */
	public PersistentObject createFromString(String code, DBConnection dbConnection){
		if (code == null) {
			return null;
		}
		
		String[] ci = code.split(StringConstants.DOUBLECOLON);
		if (ci.length != 2)
			return null;
		
		PersistentObject ret = null;
		// try to resolve factory from cache
		PersistentObjectFactory persistentObjectFactory = poFactoryCache.get(ci[0]);
		if (persistentObjectFactory != null) {
			ret = persistentObjectFactory.createFromString(code);
			ret.setDBConnection(dbConnection);
			return ret;
		}
		
		try {
			Class clazz = Class.forName(ci[0]);
			Method load = clazz.getMethod("load", new Class[] {
				String.class
			});
			ret = (PersistentObject) (load.invoke(null, new Object[] {
				ci[1]
			}));
			ret.setDBConnection(dbConnection);
			return ret;
		} catch (ClassNotFoundException ex) {
			List<PersistentObjectFactory> contributedFactories =
				Extensions.getClasses(ExtensionPointConstantsData.PERSISTENT_REFERENCE, CLASS);
			for (PersistentObjectFactory po : contributedFactories) {
				ret = po.createFromString(code);
				if (ret != null) {
					// found a responsible factory, cache it
					poFactoryCache.put(ci[0], po);
					ret.setDBConnection(dbConnection);
					return ret;
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return ret;
		}
		return ret;
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		
	}
	
	/**
	 * Ask the plug-in local classloader to return an instance of a Class as given by the first part
	 * of the storeToString (e.g. ch.elexis.eigenartikel.Eigenartikel::392393253959)
	 * 
	 * @param fullyQualifiedClassName
	 * @return Class if found, else null
	 */
	public Class getClassforName(String fullyQualifiedClassName){
		Class ret = null;
		try {
			ret = Class.forName(fullyQualifiedClassName);
			return ret;
		} catch (ClassNotFoundException e) {
			return ret;
		}
	}
}
