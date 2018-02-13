/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Tree;

public class GlobalEventDispatcher implements IPartListener2 {
	private static GlobalEventDispatcher theInstance;
	private final ConcurrentHashMap<IWorkbenchPart, LinkedList<IActivationListener>> activationListeners;
	private final GlobalListener globalListener = new GlobalListener();
	
	private GlobalEventDispatcher(){
		activationListeners =
			new ConcurrentHashMap<IWorkbenchPart, LinkedList<IActivationListener>>();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(this);
		
	}
	
	public static synchronized GlobalEventDispatcher getInstance(){
		if (theInstance == null) {
			theInstance = new GlobalEventDispatcher();
		}
		return theInstance;
	}
	
	/**
	 * Einen Standardlistener holen, der ISelectionEvents von StructuredViewers der Workbench holt
	 * und an GlobalEvents weiterleitet.
	 * 
	 * @return
	 */
	public GlobalListener getDefaultListener(){
		return globalListener;
	}
	
	/**
	 * Add a listener that will be informed as a View gets activated or deactivated, and becomes
	 * visible or invisible.
	 * 
	 * @param l
	 *            the Activationlistener. If a listener is added twice, it will be called twice.
	 * @param part
	 *            The workbench part to observe
	 */
	public static void addActivationListener(final IActivationListener l,
		final IWorkbenchPart part){
		LinkedList<IActivationListener> list = getInstance().activationListeners.get(part);
		if (list == null) {
			list = new LinkedList<IActivationListener>();
			getInstance().activationListeners.put(part, list);
		}
		list.add(l);
	}
	
	/**
	 * Remove an activationlistener. If the same listener has been added more than once, only one
	 * call will be removed.
	 * 
	 * @param l
	 *            The listener to remove. If no such listener was added, nothing happens
	 * @param part
	 *            the worbench part this listener was attached to. If no such par exists, nothing
	 *            happens
	 */
	public static void removeActivationListener(final IActivationListener l,
		final IWorkbenchPart part){
		LinkedList<IActivationListener> list = getInstance().activationListeners.get(part);
		if (list != null) {
			list.remove(l);
		}
	}
	
	public void partActivated(final IWorkbenchPartReference partRef){
		LinkedList<IActivationListener> list = activationListeners.get(partRef.getPart(false));
		if (list != null) {
			for (IActivationListener l : list) {
				l.activation(true);
			}
		}
	}
	
	public void partBroughtToTop(final IWorkbenchPartReference partRef){
		// partActivated(partRef);
		
	}
	
	public void partClosed(final IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	public void partDeactivated(final IWorkbenchPartReference partRef){
		LinkedList<IActivationListener> list = activationListeners.get(partRef.getPart(false));
		if (list != null) {
			for (IActivationListener l : list) {
				l.activation(false);
			}
		}
		
	}
	
	public void partOpened(final IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	public void partHidden(final IWorkbenchPartReference partRef){
		LinkedList<IActivationListener> list = activationListeners.get(partRef.getPart(false));
		if (list != null) {
			for (IActivationListener l : list) {
				l.visible(false);
			}
		}
		
	}
	
	public void partVisible(final IWorkbenchPartReference partRef){
		LinkedList<IActivationListener> list = activationListeners.get(partRef.getPart(false));
		if (list != null) {
			for (IActivationListener l : list) {
				l.visible(true);
			}
		}
		
	}
	
	public void partInputChanged(final IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	};
	
	private static class GlobalListener implements ISelectionChangedListener {
		
		public void selectionChanged(final SelectionChangedEvent event){
			StructuredSelection sel = (StructuredSelection) event.getSelection();
			
			Object[] obj = sel.toArray();
			if ((obj != null) && (obj.length != 0)) {
				if (obj[0] instanceof PersistentObject) {
					ElexisEventDispatcher.fireSelectionEvent((PersistentObject) obj[0]);
				} else if (obj[0] instanceof Tree) {
					Tree t = (Tree) obj[0];
					if (t.contents instanceof PersistentObject) {
						ElexisEventDispatcher.fireSelectionEvent((PersistentObject) t.contents);
					}
				}
			}
		}
	}
}
