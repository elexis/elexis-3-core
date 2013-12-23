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

package ch.elexis.core.data.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import ch.elexis.core.data.constants.ExtensionPointConstantsData;
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
		LinkedList<IConfigurationElement> ret = new LinkedList<IConfigurationElement>();
		IExtensionRegistry exr = Platform.getExtensionRegistry();
		IExtensionPoint exp = exr.getExtensionPoint(ext);
		if (exp != null) {
			IExtension[] extensions = exp.getExtensions();
			for (IExtension ex : extensions) {
				IConfigurationElement[] elems = ex.getConfigurationElements();
				for (IConfigurationElement el : elems) {
					ret.add(el);
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
	@SuppressWarnings("unchecked")//$NON-NLS-1$
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
	@SuppressWarnings("unchecked")//$NON-NLS-1$
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
					ExHandler.handle(e);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Shortcut für getClasses(getExtensions(extension),points);
	 */
	public static List getClasses(String extension, String points){
		return getClasses(getExtensions(extension), points, true);
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
	 * We sort of replicate the OSGi Service Registry, but place it on top of the Extension point
	 * system.
	 * 
	 * A Plugin can publish a service by accessing the ExtensionPoint ch.elexis.ServiceRegistry and
	 * defining a service with an arbitrary name. It must offer an Object declared as "actor" that
	 * performs the service. The Methods to use must be documented. (@see executeService)
	 * 
	 * A different plugin can implement the same Service "better" by using the same name but
	 * declaring a higher "value" A Client can retrieve and use Services through
	 * isServiceAvailable() and findBestService()
	 * 
	 * @param name
	 *            name of the service to load
	 * @param variant
	 *            the variant wanted. Can be null to indicate don't mind
	 * @return the actor-Object that the "best" Service with this name offers or null if no service
	 *         with the given name could be loaded.
	 */
	public static Object findBestService(String name, String variant){
		int value = Integer.MIN_VALUE;
		IConfigurationElement best = null;
		List<IConfigurationElement> services = getExtensions(ExtensionPointConstantsData.SERVICE_REGISTRY);
		for (IConfigurationElement ic : services) {
			String nam = ic.getAttribute("name");
			if (nam.equalsIgnoreCase(name)) {
				if (variant != null) {
					String var = ic.getAttribute("variant");
					if (var == null || (!var.equalsIgnoreCase(variant))) {
						continue;
					}
				}
				String val = ic.getAttribute("value");
				if (val != null) {
					int ival = Integer.parseInt(val);
					if (ival > value) {
						value = ival;
						best = ic;
					}
				}
			}
		}
		if (best == null) {
			return null;
		} else {
			try {
				return best.createExecutableExtension("actor");
				
			} catch (CoreException e) {
				ExHandler.handle(e);
				return null;
			}
		}
		
	}
	
	public static List<Object> getServices(String name, String variant){
		List<IConfigurationElement> services = getExtensions(ExtensionPointConstantsData.SERVICE_REGISTRY);
		List<Object> ret = new ArrayList<Object>();
		for (IConfigurationElement ic : services) {
			String nam = ic.getAttribute("name");
			if (nam.equalsIgnoreCase(name)) {
				if (variant != null) {
					String var = ic.getAttribute("variant");
					if (var == null || (!var.equalsIgnoreCase(variant))) {
						continue;
					}
				}
				try {
					ret.add(ic.createExecutableExtension("actor"));
				} catch (CoreException e) {
					ExHandler.handle(e);
				}
			}
		}
		return ret;
		
	}
	
	/**
	 * Shortcut for findBestService(name,null)
	 */
	public static Object findBestService(String name){
		return findBestService(name, null);
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
	
	/**
	 * Ask whether a service is available. The call is cheap because no Object will be
	 * instantaniated. Note: If this call returns true, a call to findBestService() might still
	 * return null, because a Service might have gone unavailable in the meantime.
	 * 
	 * @param name
	 *            the name of the service to find.
	 * @return true if at least one implementation of a service with the given name is registered
	 */
	public static boolean isServiceAvailable(String name){
		List<IConfigurationElement> services = getExtensions(ExtensionPointConstantsData.SERVICE_REGISTRY);
		for (IConfigurationElement ic : services) {
			String nam = ic.getAttribute("name");
			if (nam.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}
