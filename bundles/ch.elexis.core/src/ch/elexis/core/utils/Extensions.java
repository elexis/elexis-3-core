/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.utils;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import ch.elexis.core.jdt.NonNull;
import ch.rgw.tools.ExHandler;

/**
 * Vereinfachung der Handhabung von Extensions. Verschidene statische Methoden zum Auflisten von
 * Extensionpoint.-clients
 * 
 * 2008: Implementation eines Service-Providers
 * 
 * @author gerry
 * 
 */
public class Extensions {
	
	/**
	 * EIne Liste von IConfigurationElements (=komplette Definition) liefern, die an einem
	 * bestimmten Extensionpoint hängen
	 * 
	 * @param ext
	 *            Name des Extensionpoints
	 */
	public static List<IConfigurationElement> getExtensions(String ext){
		return getExtensions(ext, null);
	}
	
	/**
	 * Eine Liste von IConfigurationElements (=komplette Definition) liefern, die an einem
	 * bestimmten Extensionpoint hängen, und den entsprechenden Elementnamen haben
	 * 
	 * @param ext
	 * @param elementName
	 * @return
	 * @since 3.3
	 */
	public static List<IConfigurationElement> getExtensions(String ext, String elementName){
		List<IConfigurationElement> ret = new LinkedList<IConfigurationElement>();
		IExtensionRegistry exr = Platform.getExtensionRegistry();
		IExtensionPoint exp = exr.getExtensionPoint(ext);
		if (exp != null) {
			IExtension[] extensions = exp.getExtensions();
			for (IExtension ex : extensions) {
				IConfigurationElement[] elems = ex.getConfigurationElements();
				for (IConfigurationElement el : elems) {
					if (elementName != null) {
						if (elementName.equals(el.getName())) {
							ret.add(el);
						}
					} else {
						ret.add(el);
					}
				}
			}
			
		}
		return ret;
	}
	
	/**
	 * Eine Liste von bereits initialisierten Klassen liefern, die an einem bestimmten parameter
	 * eines bestimmten Extensionpoints hängen
	 * 
	 * @param list
	 *            eine Liste, wie von getExtension geliefert
	 * @param points
	 *            Name der Klasse
	 * @return eine Liste der konstruierten Klassen
	 * @deprecated Use {@link #getClasses(List<IConfigurationElement>,String,boolean)} instead
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static List getClasses(List<IConfigurationElement> list, String points){
		return getClasses(list, points, true);
	}
	
	/**
	 * Eine Liste von bereits initialisierten Klassen liefern, die an einem bestimmten parameter
	 * eines bestimmten Extensionpoints hängen
	 * 
	 * @param list
	 *            eine Liste, wie von getExtension geliefert
	 * @param points
	 *            Name der Klasse
	 * @param bMandatory
	 *            false: do not handle exceptions
	 * @return eine Liste der konstruierten Klassen
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static List getClasses(List<IConfigurationElement> list, String points,
		boolean bMandatory){
		List ret = new LinkedList();
		for (IConfigurationElement el : list) {
			try {
				Object o = el.createExecutableExtension(points);
				if (o != null) {
					ret.add(o);
				}
			} catch (CoreException e) {
				if (bMandatory) {
					ExHandler.handle(el.getName() + ": " + points, e);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Shortcut für getClasses(getExtensions(extension),points);
	 */
	public static List getClasses(String extension, String points){
		return getClasses(extension, points, null, null);
	}
	
	/**
	 * Get the class extensions of all elements of an extension point matching the elementName and
	 * points.
	 * 
	 * @param extension
	 * @param elementName
	 * @param points
	 * @return
	 * 
	 * @since 3.3
	 */
	public static List getClasses(String extension, String elementName, String points){
		return getClasses(extension, elementName, points, null, null);
	}
	
	/**
	 * 
	 * @param extension
	 * @param points
	 * @param idParam
	 *            id parameter on the extension point to filter against
	 * @param idValue
	 *            key matching idParam
	 * @return instantiated classes of a defined extension point
	 * @since 3.2
	 */
	public static @NonNull List getClasses(String extension, String points, String idParam,
		String idValue){
		return getClasses(extension, null, points, idParam, idValue);
	}
	
	/**
	 * Get the class extensions of all elements of an extension point matching the elementName,
	 * points and idParam with idValue.
	 * 
	 * @param extension
	 * @param elementName
	 * @param points
	 * @param idParam
	 * @param idValue
	 * @return
	 * 
	 * @since 3.3
	 */
	public static @NonNull List getClasses(String extension, String elementName, String points,
		String idParam, String idValue){
		List<IConfigurationElement> extensions = getExtensions(extension, elementName);
		if (idParam != null && idValue != null) {
			List<IConfigurationElement> filteredExtensions =
				extensions.stream().filter(p -> idValue.equalsIgnoreCase(p.getAttribute(idParam)))
					.collect(Collectors.toList());
			return getClasses(filteredExtensions, points, true);
		}
		return getClasses(extensions, points, true);
	}
	
	/**
	 * Eine Liste von Werten liefern, die ein bestimmtest Attribut hat
	 * 
	 * @param list
	 * @param attr
	 * @return
	 */
	public static List<String> getStrings(List<IConfigurationElement> list, String attr){
		List<String> ret = new LinkedList<String>();
		for (IConfigurationElement el : list) {
			ret.add(el.getAttribute(attr));
		}
		return ret;
	}
	
	public static List<String> getStrings(String ext, String attr){
		return getStrings(getExtensions(ext), attr);
	}
	
	/**
	 * Execute a method of the service actor, that is known by name and signature
	 * 
	 * @param service
	 *            The service actor as returned by findBestService()
	 * @param method
	 *            the name of the method to be called
	 * @param types
	 *            the parameter types
	 * @param params
	 *            the parameters
	 * @return an Object that ist implementation dependent or null if the method call failed. It is
	 *         recommended that an actor returns a ch.elexis.Result to allow error handling.
	 */
	public static Object executeService(Object service, String method, Class[] types,
		Object[] params){
		try {
			Method m = service.getClass().getMethod(method, types);
			return m.invoke(service, params);
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
}
