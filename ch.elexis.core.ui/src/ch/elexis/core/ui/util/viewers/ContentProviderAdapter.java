/*******************************************************************************
 * Copyright (c) 2010-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util.viewers;

import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentProviderAdapter implements IStructuredContentProvider {
	public static interface UpdateListener extends EventListener {
		public void updatedContents(IStructuredContentProvider contentProvider);
	}
	
	private List<UpdateListener> updateListeners;
	
	/**
	 * Add a listener that will be informed if this ContentProvider is activated
	 * 
	 * @param listener
	 */
	public void addUpdateListener(UpdateListener listener){
		if (updateListeners == null) {
			updateListeners = new LinkedList<UpdateListener>();
		}
		updateListeners.add(listener);
	}
	
	/**
	 * remove a previously added UpdateListener. If no such listener exists or if it was removed
	 * earlier already, nothing will happen.
	 * 
	 * @param listener
	 */
	public void removeUpdateListener(UpdateListener listener){
		if (updateListeners != null) {
			updateListeners.remove(listener);
		}
	}
	
	@Override
	public void dispose(){
		if (updateListeners != null) {
			updateListeners.clear();
			updateListeners = null;
		}
	}
	
	public void fireUpdateEvent(){
		for (UpdateListener l : updateListeners) {
			l.updatedContents(this);
		}
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		// TODO Auto-generated method stub
		return null;
	}
	
}
