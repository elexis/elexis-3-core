/*******************************************************************************
 * Copyright (c) 2007-2010, Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich and D. Lutz - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import java.util.Hashtable;

import org.eclipse.jface.viewers.IFilter;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.events.ElexisEventDispatcher;

public class ObjectFilterRegistry {
	
	private static ObjectFilterRegistry theInstance;
	private final Hashtable<Class<? extends PersistentObject>, IObjectFilterProvider> hash =
		new Hashtable<Class<? extends PersistentObject>, IObjectFilterProvider>();
	
	private ObjectFilterRegistry(){}
	
	public static ObjectFilterRegistry getInstance(){
		if (theInstance == null) {
			theInstance = new ObjectFilterRegistry();
		}
		return theInstance;
	}
	
	public synchronized void registerObjectFilter(final Class<? extends PersistentObject> clazz,
		final IObjectFilterProvider provider){
		IObjectFilterProvider old = hash.get(clazz);
		if (old != null) {
			old.deactivate();
		}
		hash.put(clazz, provider);
		provider.activate();
		ElexisEventDispatcher.reload(clazz);
	}
	
	public void unregisterObjectFilter(final Class<? extends PersistentObject> clazz,
		final IObjectFilterProvider provider){
		hash.remove(clazz);
		provider.deactivate();
		ElexisEventDispatcher.reload(clazz);
	}
	
	public IFilter getFilterFor(final Class<? extends PersistentObject> clazz){
		IObjectFilterProvider prov = hash.get(clazz);
		if (prov != null) {
			return prov.getFilter();
		}
		return null;
	}
	
	public interface IObjectFilterProvider {
		public void activate();
		
		public void deactivate();
		
		public String getId();
		
		public void changed();
		
		public IFilter getFilter();
	}
	
}
