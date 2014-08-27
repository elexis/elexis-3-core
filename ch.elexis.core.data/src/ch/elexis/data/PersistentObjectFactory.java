/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.PersistenceException;
import ch.rgw.tools.ExHandler;

public class PersistentObjectFactory implements IExecutableExtension {
	
	/**
	 * Ein Objekt als Schablone eines beliebigen abgeleiteten Typs erstellen, ohne es in die
	 * Datenbank einzutragen. Wenn der Programmkern kein Objekt dieser Klasse erstellen kann, werden
	 * der Reihe nach alle Plugins abgeklappert, die eine PersistentObjectFactory deklariert haben.
	 * 
	 * @param typ
	 *            Der gewünschte Subtyp von PersistentObject
	 * 
	 * @return ein unabhängiges Objekt des gewünschten Typs oder null
	 * 
	 * @throws PersistenceException
	 */
	@SuppressWarnings("unchecked")
	public PersistentObject createTemplate(Class typ){
		
		try {
			return (PersistentObject) typ.newInstance();
		} catch (IllegalAccessException ex) {
			List<PersistentObjectFactory> exts =
				Extensions.getClasses(ExtensionPointConstantsData.PERSISTENT_REFERENCE, "Class");
			for (PersistentObjectFactory po : exts) {
				PersistentObject ret = po.doCreateTemplate(typ);
				if (ret != null) {
					return ret;
				}
			}
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"create: Konnte Objekt nicht erstellen " + ex.getMessage(), ex,
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
	
	private HashMap<String, PersistentObjectFactory> poFactoryCache = new HashMap<>();
	
	/**
	 * Ein Objekt einer beliebigen abgeleiteten Klasse anhand des Pseudoserialisiercodes erstellen.
	 * Wenn das Objekt vom Programmkern nicht erstellt werden kann, werden der Reihe nach alle
	 * Plugins abgeklappert, die eine PersistentObjectFactory deklariert haben.
	 * 
	 * @param code
	 *            der String, der das Objekt beschreibt
	 * @return das erstellte Objekt oder null, wenn aus dem übergebenen Code kein Objekt erstellt
	 *         werden konnte.
	 */
	@SuppressWarnings("unchecked")
	public PersistentObject createFromString(String code){
		if (code == null) {
			return null;
		}
		
		String[] ci = code.split(StringConstants.DOUBLECOLON);
		if (ci.length != 2)
			return null;
		
		// try to resolve factory from cache
		PersistentObjectFactory persistentObjectFactory = poFactoryCache.get(ci[0]);
		if (persistentObjectFactory != null && persistentObjectFactory != this) {
			return persistentObjectFactory.createFromString(code);
		}
		
		try {
			Class clazz = Class.forName(ci[0]);
			Method load = clazz.getMethod("load", new Class[] {
				String.class
			});
			// set this object factory as responsible
			poFactoryCache.put(ci[0], this);
			return (PersistentObject) (load.invoke(null, new Object[] {
				ci[1]
			}));
		} catch (ClassNotFoundException ex) {
			List<PersistentObjectFactory> exts =
				Extensions.getClasses(ExtensionPointConstantsData.PERSISTENT_REFERENCE, "Class");
			for (PersistentObjectFactory po : exts) {
				PersistentObject ret = po.createFromString(code);
				if (ret != null) {
					// found a responsible factory, cache it
					poFactoryCache.put(ci[0], po);
					return ret;
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		return null;
		
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
